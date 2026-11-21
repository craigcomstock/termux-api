F=$TMPDIR/mpv-meta.txt; : > $F
SOCK=$TMPDIR/mpv.sock
mpv --input-ipc-server=$SOCK "$@" &
tail -n0 -F $F | /data/data/com.termux/files/usr/libexec/termux-api MediaButton |
while read -r k; do
  case $k in
    KEYCODE_MEDIA_PLAY_PAUSE|KEYCODE_MEDIA_PLAY|KEYCODE_MEDIA_PAUSE|KEYCODE_HEADSETHOOK)
      echo cycle pause | socat - $SOCK ;;
  esac
done
