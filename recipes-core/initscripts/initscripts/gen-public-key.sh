# INIT INFO
# Provides:          Gen public key from /etc/dropbear/dropbear_rsa_host_key
# Required-Start:    mountvirtfs $local_fs
# Required-Stop:     $local_fs
# Should-Start:      dropbearkey
# Should-Stop:       
# Default-Start:     S
# Default-Stop:      0 6
# Short-Description: Raise network interfaces.
### END INIT INFO

PATH="/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin"

case "$1" in
start)
	if [ -f /var/lib/dropbear/id_rsa.pub ]
	then
		echo "Public Key already exists."
	else
		echo "Creating Public Key.."
		dropbearkey -y -f /var/lib/dropbear/dropbear_rsa_host_key | grep "^ssh-rsa" > /var/lib/dropbear/id_rsa.pub
		echo "DONE!"
	fi
	;;
stop)
	;;

*)
	echo "Usage: /etc/init.d/networking {start|stop}"
	exit 1
	;;
esac

exit 0

