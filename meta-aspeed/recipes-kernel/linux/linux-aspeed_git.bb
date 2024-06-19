KBRANCH ?= "dev-6.6"
LINUX_VERSION ?= "6.6.34"

SRCREV="ba2929159000dc7015cc01cdf7bb72542e19952a"

require linux-aspeed.inc

SRC_URI += "file://defconfig \
           file://rsa_oem_fitimage_key.key;sha256sum=eeb4ff2ebbfbd97b6254fe6dbaeea41067e54c65176c233ec7b2ab2decf1ddcd \
           file://rsa_oem_fitimage_key.crt;sha256sum=45f5a55497cce8040999bf9f3214d471ac7b83ab7acef41c4425a34662e8372e \
           ${@bb.utils.contains('MACHINE_FEATURES', 'tpm2', 'file://tpm/tpm2.scc file://tpm/tpm2.cfg', '', d)} \
           ${@bb.utils.contains_any('DISTRO_FEATURES', 'obmc-static-norootfs obmc-ubi-fs', 'file://ubi/ubi.scc file://ubi/ubi.cfg', '', d)} \
           file://systemd-bootchart.cfg \
           file://0001-feat-dts-modify-aspeed-ast2500-evb.dts.patch \
           "
