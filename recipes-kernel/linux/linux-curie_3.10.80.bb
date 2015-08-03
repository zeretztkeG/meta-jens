# Copyright (C) 2014 Shanghai Zhixing Information Technology Co.Ltd
# Copyright (C) 2014-2015 Rademacher Elektronik GmbH

SUMMARY = "Linux Kernel for Curie Board"
DESCRIPTION = "Linux Kernel for Curie Board"

require recipes-kernel/linux/linux-imx.inc
require recipes-kernel/linux/linux-dtb.inc

DEPENDS += "lzop-native bc-native u-boot-curie"

REV="aa863e040e9c9a14addb9fbed1c460c20f21c6f9"
SRCREPO="rdm-dev"
SRCBRANCH = "curie_3.10.81_1.0.0_ga"
LOCALVERSION = "+curie"

SRC_URI = "git://github.com/${SRCREPO}/linux-curie.git;branch=${SRCBRANCH};rev=${REV} \
           file://0001-Importing-rtl8189es_4.3.10.1-driver.patch \
           file://0002-include-rtl8189es-driver-in-kernel-build.patch \
           file://0003-Add-platform-specific-modifications-for-Curie.patch \
           file://0004-don-t-printout-debug-message-when-DBG-is-off.patch \
	   file://overlayfs-v18.patch \
	   file://defconfig \
	   file://bootscript.mmc \
	   file://bootscript.nfs \
	   file://bootscript.usb \
          "

BAD_URI = "file://0006-mmc-An-optional-entry-in-imx-usdhc-for-max_freq.patch \
	   file://0007-dt-Set-the-max-frequency-for-the-mmc-controllers.patch \
          "

# patches for curie
COMPATIBLE_MACHINE = "(curie)"

do_install_append () {
    SDCARD_IMAGE=0
    test "${WANTED_ROOT_DEV}" = "sd" && SDCARD_IMAGE=1
    sed -i -e "s/@UBOOT_LOADADDRESS[@]/${UBOOT_LOADADDRESS}/g" -e "s/@UBOOT_FDTADDRESS[@]/${UBOOT_FDTADDRESS}/g" \
           -e "s/@UBOOT_MMC_DEV[@]/${UBOOT_MMC_DEV}/g" -e "s/@SDCARD_IMAGE[@]/${SDCARD_IMAGE}/g" \
           -e "s/@KERNEL_MMC_DEV[@]/${KERNEL_MMC_DEV}/g" \
           -e "s/@KERNEL_IMAGETYPE[@]/${KERNEL_IMAGETYPE}/g" -e "s/@KERNEL_DEVICETREE[@]/${KERNEL_DEVICETREE}/g" \
	   -e "s/@MACHINE[@]/${MACHINE}/g" -e "s/@BRANCH[@]/${METADATA_BRANCH}/g" \
	 ${WORKDIR}/bootscript.mmc ${WORKDIR}/bootscript.nfs ${WORKDIR}/bootscript.usb
    uboot-mkimage -T script -C none -n 'Curie Script' -d ${WORKDIR}/bootscript.mmc ${D}/boot/bootscript.mmc
    uboot-mkimage -T script -C none -n 'Curie Script' -d ${WORKDIR}/bootscript.nfs ${D}/boot/bootscript.nfs
    uboot-mkimage -T script -C none -n 'Curie Script' -d ${WORKDIR}/bootscript.usb ${D}/boot/bootscript.usb

    echo "options 8189es rtw_power_mgnt=0" >${WORKDIR}/8189es.conf
    echo "blacklist 8189es" >${WORKDIR}/blacklist-8189es.conf
    echo "blacklist cfg80211" >${WORKDIR}/blacklist-cfg80211.conf
    echo "blacklist ahci_imx" >${WORKDIR}/blacklist-ahci_imx.conf
    install -d ${D}${sysconfdir}/modprobe.d/
    install -m 644 ${WORKDIR}/8189es.conf ${D}${sysconfdir}/modprobe.d/
    install -m 644 ${WORKDIR}/blacklist-8189es.conf ${D}${sysconfdir}/modprobe.d/
    install -m 644 ${WORKDIR}/blacklist-cfg80211.conf ${D}${sysconfdir}/modprobe.d/
    install -m 644 ${WORKDIR}/blacklist-ahci_imx.conf ${D}${sysconfdir}/modprobe.d/
}

do_deploy_append () {
    set -x
    cp ${D}/boot/bootscript.mmc ${DEPLOYDIR}/bootscript.mmc-${DATETIME}
    cp ${D}/boot/bootscript.nfs ${DEPLOYDIR}/bootscript.nfs-${DATETIME}
    cp ${D}/boot/bootscript.usb ${DEPLOYDIR}/bootscript.usb-${DATETIME}
    ln -sf bootscript.mmc-${DATETIME} ${DEPLOYDIR}/bootscript.mmc
    ln -sf bootscript.nfs-${DATETIME} ${DEPLOYDIR}/bootscript.nfs
    ln -sf bootscript.usb-${DATETIME} ${DEPLOYDIR}/bootscript.usb

    DEFAULT_BOOTSCRIPT="bootscript.mmc-${DATETIME}"
    test -f ${DEPLOYDIR}/bootscript.${WANTED_ROOT_DEV}-${DATETIME} && DEFAULT_BOOTSCRIPT="bootscript.${WANTED_ROOT_DEV}-${DATETIME}"
    ln -sf ${DEFAULT_BOOTSCRIPT} ${DEPLOYDIR}/bootscript
    : # exit 0
}

FILES_kernel-module-8189es += "${sysconfdir}/modprobe.d/blacklist-8189es.conf"
FILES_kernel-module-ahci-imx += "${sysconfdir}/modprobe.d/blacklist-ahci_imx.conf"
FILES_kernel-module-cfg80211 += "${sysconfdir}/modprobe.d/blacklist-cfg80211.conf"
FILES_kernel-image += "/boot/bootscript.mmc /boot/bootscript.nfs /boot/bootscript.usb"
