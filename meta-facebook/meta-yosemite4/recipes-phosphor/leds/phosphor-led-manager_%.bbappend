SYSTEMD_SERVICE:${PN}-faultmonitor:remove:yosemite4 = "obmc-fru-fault-monitor.service"
SYSTEMD_LINK:${PN}:remove:yosemite4 = "../obmc-led-group-start@.service:multi-user.target.wants/obmc-led-group-start@bmc_booted.service"
SYSTEMD_LINK:${PN}:remove:yosemite4 = "${@compose_list_zip(d, 'FMT', 'CHASSIS_TARGETS', 'STATES')}"
SYSTEMD_LINK:${PN}:remove:yosemite4 = "${@compose_list(d, 'CHASSIS_LED_BLACKOUT_FMT', 'OBMC_CHASSIS_INSTANCES' )}"
SYSTEMD_OVERRIDE:${PN}:remove:yosemite4 = "bmc_booted.conf:obmc-led-group-start@bmc_booted.service.d/bmc_booted.conf"

FAN_INSTANCES="0 1 2 3 4 5 6 7 8 9 10 11"
FAN_OK_START_INSTFMT="obmc-led-group-start@.service:obmc-led-group-start@fan{0}_ok.service"
SYSTEMD_LINK:${PN} += "${@compose_list_zip(d, 'FAN_OK_START_INSTFMT', 'FAN_INSTANCES')}"

FAN_FAIL_START_INSTFMT="obmc-led-group-start@.service:obmc-led-group-start@fan{0}_fail.service"
SYSTEMD_LINK:${PN} += "${@compose_list_zip(d, 'FAN_FAIL_START_INSTFMT', 'FAN_INSTANCES')}"

FAN_OK_STOP_INSTFMT="obmc-led-group-stop@.service:obmc-led-group-stop@fan{0}_ok.service"
SYSTEMD_LINK:${PN} += "${@compose_list_zip(d, 'FAN_OK_STOP_INSTFMT', 'FAN_INSTANCES')}"

FAN_FAIL_STOP_INSTFMT="obmc-led-group-stop@.service:obmc-led-group-stop@fan{0}_fail.service"
SYSTEMD_LINK:${PN} += "${@compose_list_zip(d, 'FAN_FAIL_STOP_INSTFMT', 'FAN_INSTANCES')}"
