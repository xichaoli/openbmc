SUMMARY = "Configures the gbmc bridge and filter rules"
PR = "r1"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

inherit systemd

GBMC_DHCP_RELAY ??= "${@'' if int(d.getVar('FLASH_SIZE')) < 65536 else '1'}"

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"
SRC_URI += " \
  file://-bmc-gbmcbr.netdev \
  file://-bmc-gbmcbr.network.in \
  file://-bmc-gbmcbrdummy.netdev \
  file://-bmc-gbmcbrdummy.network \
  file://+-bmc-gbmcbrusb.network \
  file://50-gbmc-br.rules \
  file://gbmc-br-ula.sh \
  file://gbmc-br-from-ra.sh \
  file://gbmc-br-hostname.sh \
  file://gbmc-br-hostname.service \
  file://gbmc-br-ra.sh.in \
  file://gbmc-br-ra.service \
  file://gbmc-br-gw-src.sh \
  file://gbmc-br-nft.sh \
  file://gbmc-br-dhcp.sh \
  file://50-gbmc-psu-hardreset.sh.in \
  file://51-gbmc-reboot.sh \
  file://gbmc-br-dhcp@.service \
  file://gbmc-br-dhcp-term.sh \
  file://gbmc-br-dhcp-term.service \
  file://gbmc-br-lib.sh \
  file://gbmc-br-load-ip.service \
  file://gbmc-start-dhcp.sh \
  file://50-gbmc-br-cn-redirect.rules \
  ${@'' if d.getVar('GBMC_DHCP_RELAY') != '1' else 'file://gbmc-br-dhcrelay@.service'} \
  ${@'' if d.getVar('GBMC_DHCP_RELAY') != '1' else 'file://gbmc-br-dhcrelay.sh'} \
  ${@'' if d.getVar('GBMC_DHCP_RELAY') != '1' else 'file://50-gbmc-br-dhcp.rules'} \
  ${@'' if d.getVar('GBMC_DHCP_RELAY') != '1' else 'file://-bmc-gbmcdhcp.netdev'} \
  ${@'' if d.getVar('GBMC_DHCP_RELAY') != '1' else 'file://-bmc-gbmcdhcp.network'} \
  ${@'' if d.getVar('GBMC_DHCP_RELAY') != '1' else 'file://-bmc-gbmcbrdhcp.netdev'} \
  ${@'' if d.getVar('GBMC_DHCP_RELAY') != '1' else 'file://-bmc-gbmcbrdhcp.network'} \
  "

FILES:${PN}:append = " \
  ${datadir}/gbmc-ip-monitor \
  ${datadir}/gbmc-br-dhcp \
  ${datadir}/gbmc-br-lib.sh \
  ${systemd_system_unitdir} \
  ${systemd_unitdir}/network \
  ${sysconfdir}/nftables \
  "

RDEPENDS:${PN}:append = " \
  bash \
  dhcp-done \
  gbmc-ip-monitor \
  gbmc-net-common \
  network-sh \
  ndisc6-rdisc6 \
  nftables-systemd \
  "

SYSTEMD_SERVICE:${PN} += " \
  gbmc-br-hostname.service \
  gbmc-br-dhcp-term.service \
  gbmc-br-load-ip.service \
  gbmc-br-ra.service \
  "

GBMC_BR_MAC_ADDR ?= ""

# Enables the assignment of IP address and hostname by discovering the
# machine name and BMC prefix from another BMC on the bridge network.
# This is intended only to be used when there is a single expansion tray
# on the BMC network. If more than one machine uses this feature with the
# same offset in the same machine network, it will collide with others.
# A value of 0 implies that this feature is disabled.
GBMC_BR_FIXED_OFFSET ?= "0"

# Generated via https://cd34.com/rfc4193/ based on a MAC from a machine I own
# and we allocated it downstream. Intended to only be used within a complete
# system of multiple network endpoints.
GBMC_ULA_PREFIX = "fdb5:0481:10ce:0"

# coordinated powercycle
GBMC_COORDINATED_POWERCYCLE ?= "true"

# Allow machines to upgrade all netboot warm reboots into powercyles in case
# they have stability issues performing them. Disable this feature by default.
GBMC_NETBOOT_UPGRADE_REBOOT ?= "0"

def mac_to_eui64(mac):
  if not mac:
    return ''
  b = [int(c, 16) for c in mac.split(':')]
  b[0] ^= 2
  b.insert(3, 0xfe)
  b.insert(3, 0xff)
  idx = range(0, len(b)-1, 2)
  return ':'.join([format((b[i] << 8) + b[i+1], '04x') for i in idx])

def macs_to_eui64(macs):
  return ' '.join([mac_to_eui64(mac) for mac in macs.split(' ')])

GBMC_BRIDGE_INTFS ?= ""

ethernet_bridge_install() {
  # install udev rules if any
  if [ -z "${GBMC_BRIDGE_INTFS}"]; then
    return
  fi
  cat /dev/null > ${UNPACKDIR}/-ether-bridge.network
  echo "[Match]" >> ${UNPACKDIR}/-ether-bridge.network
  echo "Name=${GBMC_BRIDGE_INTFS}" >>  ${UNPACKDIR}/-ether-bridge.network
  echo "[Network]" >> ${UNPACKDIR}/-ether-bridge.network
  echo "Bridge=gbmcbr" >> ${UNPACKDIR}/-ether-bridge.network

  install -d ${D}/${sysconfdir}/systemd/network
  install -m 0644 ${UNPACKDIR}/-ether-bridge.network ${D}/${sysconfdir}/systemd/network/
}

do_install() {
  netdir=${D}${systemd_unitdir}/network
  install -d -m0755 $netdir

  if [ ! -z "${GBMC_BR_MAC_ADDR}" ]; then
    eui64_list="${@macs_to_eui64(d.getVar('GBMC_BR_MAC_ADDR'))}"
    addr=""
    for eui64 in $eui64_list
    do
      addr="$addr[Address]\nAddress=${GBMC_ULA_PREFIX}:$eui64/64\nPreferredLifetime=0\n"
      addr="$addr[Address]\nAddress=fe80::$eui64/64\nPreferredLifetime=0\n"
    done
    sed -i "s,@ADDR@,$addr," ${UNPACKDIR}/-bmc-gbmcbr.network.in
  else
    sed -i '/@ADDR@/d' ${UNPACKDIR}/-bmc-gbmcbr.network.in
  fi

  ethernet_bridge_install

  install -m0644 ${UNPACKDIR}/-bmc-gbmcbr.netdev $netdir/
  install -m0644 ${UNPACKDIR}/-bmc-gbmcbr.network.in $netdir/-bmc-gbmcbr.network
  install -m0644 ${UNPACKDIR}/-bmc-gbmcbrdummy.netdev $netdir/
  install -m0644 ${UNPACKDIR}/-bmc-gbmcbrdummy.network $netdir/
  install -m0644 ${UNPACKDIR}/+-bmc-gbmcbrusb.network $netdir/

  nftables_dir=${D}${sysconfdir}/nftables
  install -d -m0755 "$nftables_dir"
  install -m0644 ${UNPACKDIR}/50-gbmc-br.rules $nftables_dir/
  install -m0644 ${UNPACKDIR}/50-gbmc-br-cn-redirect.rules $nftables_dir/

  mondir=${D}${datadir}/gbmc-ip-monitor
  install -d -m0755 "$mondir"
  install -m0644 ${UNPACKDIR}/gbmc-br-ula.sh "$mondir"/
  install -m0644 ${UNPACKDIR}/gbmc-br-from-ra.sh "$mondir"/
  install -m0644 ${UNPACKDIR}/gbmc-br-gw-src.sh "$mondir"/
  install -m0644 ${UNPACKDIR}/gbmc-br-nft.sh "$mondir"/

  install -d -m0755 ${D}${libexecdir}
  install -m0755 ${UNPACKDIR}/gbmc-br-hostname.sh ${D}${libexecdir}/
  install -m0755 ${UNPACKDIR}/gbmc-br-dhcp.sh ${D}${libexecdir}/
  install -m0755 ${UNPACKDIR}/gbmc-br-dhcp-term.sh ${D}${libexecdir}/
  install -d -m0755 ${D}${systemd_system_unitdir}
  install -m0644 ${UNPACKDIR}/gbmc-br-hostname.service ${D}${systemd_system_unitdir}/
  install -m0644 ${UNPACKDIR}/gbmc-br-dhcp@.service ${D}${systemd_system_unitdir}/
  wantdir=${D}${systemd_system_unitdir}/multi-user.target.wants
  install -d -m0755 $wantdir
  ln -sv ../gbmc-br-dhcp@.service $wantdir/gbmc-br-dhcp@gbmcbr.service
  install -m0644 ${UNPACKDIR}/gbmc-br-dhcp-term.service ${D}${systemd_system_unitdir}/
  install -m0644 ${UNPACKDIR}/gbmc-br-load-ip.service ${D}${systemd_system_unitdir}/
  install -d -m0755 ${D}${datadir}/gbmc-br-dhcp

  sed -e 's,@COORDINATED_POWERCYCLE@,${GBMC_COORDINATED_POWERCYCLE},' \
      -e 's,@UPGRADE_REBOOT@,${GBMC_NETBOOT_UPGRADE_REBOOT},' \
    ${UNPACKDIR}/50-gbmc-psu-hardreset.sh.in >${UNPACKDIR}/50-gbmc-psu-hardreset.sh
  install -m0644 ${UNPACKDIR}/50-gbmc-psu-hardreset.sh ${D}${datadir}/gbmc-br-dhcp/
  install -m0644 ${UNPACKDIR}/51-gbmc-reboot.sh ${D}${datadir}/gbmc-br-dhcp/

  install -m0644 ${UNPACKDIR}/gbmc-br-lib.sh ${D}${datadir}/

  install -d ${D}/${bindir}
  install -m0755 ${UNPACKDIR}/gbmc-start-dhcp.sh ${D}${bindir}/

  sed 's,@IP_OFFSET@,${GBMC_BR_FIXED_OFFSET},' ${UNPACKDIR}/gbmc-br-ra.sh.in >${UNPACKDIR}/gbmc-br-ra.sh
  install -m0755 ${UNPACKDIR}/gbmc-br-ra.sh ${D}${libexecdir}/
  install -m0644 ${UNPACKDIR}/gbmc-br-ra.service ${D}${systemd_system_unitdir}/

  if [ "${GBMC_DHCP_RELAY}" = 1 ]; then
    install -m0644 ${UNPACKDIR}/gbmc-br-dhcrelay@.service ${D}${systemd_system_unitdir}/
    install -m0644 ${UNPACKDIR}/gbmc-br-dhcrelay.sh "$mondir"/
    install -m0644 ${UNPACKDIR}/50-gbmc-br-dhcp.rules $nftables_dir/
    install -m0644 ${UNPACKDIR}/-bmc-gbmcdhcp.netdev $netdir/
    install -m0644 ${UNPACKDIR}/-bmc-gbmcdhcp.network $netdir/
    install -m0644 ${UNPACKDIR}/-bmc-gbmcbrdhcp.netdev $netdir/
    install -m0644 ${UNPACKDIR}/-bmc-gbmcbrdhcp.network $netdir/
  fi
}

do_rm_work:prepend() {
  # HACK: Work around broken do_rm_work not properly calling rm with `--`
  # It doesn't like filenames that start with `-`
  rm -rf -- ${UNPACKDIR}/-*
}
