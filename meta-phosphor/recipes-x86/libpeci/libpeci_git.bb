SUMMARY = "PECI Library"
DESCRIPTION = "PECI Library"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7becf906c8f8d03c237bad13bc3dac53"
SRCREV = "5302b933aca5417c3e7aabcdde4d05d2ea5c3bd3"
PACKAGECONFIG ??= ""
PACKAGECONFIG[dbus-raw-peci] = "-Draw-peci='enabled',-Draw-peci='disabled',boost sdbusplus"
PV = "0.1+git${SRCPV}"

SRC_URI = "git://github.com/openbmc/libpeci;branch=master;protocol=https"

S = "${WORKDIR}/git"
SYSTEMD_SERVICE:${PN} += "${@bb.utils.contains('PACKAGECONFIG', 'dbus-raw-peci', 'com.intel.peci.service', '', d)}"

inherit meson pkgconfig systemd
