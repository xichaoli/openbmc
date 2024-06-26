SUMMARY = "A modern Python package and dependency manager supporting the latest PEP standards"
HOMEPAGE = "https://pdm-project.org/latest/"
LICENSE = "MIT"
SECTION = "devel/python"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2eb31a2cc1a758c34b499f287dd04ef2"

SRC_URI[sha256sum] = "e282bf1caf0a083fc0fb5e89ad6f79d7c579ff52c7bb58ee8587ea3d5bb9e14a"

inherit pypi python_setuptools_build_meta

DEPENDS += " \
    python3-pdm-backend-native \
"

BBCLASSEXTEND = "native nativesdk"
