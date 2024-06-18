FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:remove:ourbmc = "git://github.com/openbmc/webui-vue.git;branch=master;protocol=https"
SRC_URI:append:ourbmc = "git://gitee.com/ourbmc/web.git;branch=master;protocol=https"
SRCREV:ourbmc = "a10d10d2065fbea40a4f555be6b1961a6e8d571a"

SRC_URI:ourbmc += " \
            file://0001-fix-Make-NPM-higher-than-version-16-available-for-co.patch \
           "

EXTRA_OENPM:ourbmc += "-- --mode ourbmc"
