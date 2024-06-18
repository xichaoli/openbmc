FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append:g220a = " file://g220a.cfg \
			 file://0005-ARM-dts-aspeed-Enable-g220a-uart-route.patch \
                         file://0006-feat-dts-Disable-ncsi-of-mac0.patch \
                         file://0007-feat-dts-Increase-the-partition-size-of-rofs.patch \
                       "
