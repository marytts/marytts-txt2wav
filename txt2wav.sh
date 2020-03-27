#!/bin/bash -eu
# - needs curl, sox and a running marytts-server
# - starts as many threads as cores are available
# - converts all textfiles in one directory to wav
# - builds the audio filenames according to the textfiles (x.txt->x.wav)
# - renames textfiles during conversion to *.work
# - renames finished textfiles to *.done
# - comments successful conversion on stdout
# - comments failed conversion on stderr

textextension='txt'
audioformat='wav'
lang='en_US'
maryserver='http://localhost:59125'
voice='cmu-slt-hsmm'

cores=`grep -c ^processor /proc/cpuinfo`
curl_data="INPUT_TYPE=TEXT&OUTPUT_TYPE=AUDIO&AUDIO=WAVE_FILE&LOCALE=$lang&VOICE=$voice"

fail() {
    echo "$*" >&2
    exit 1
}

txt2signal() {
    for textfile in *.$textextension; do
        if mv "$textfile" "$textfile.work" 2>/dev/null; then
            audiofile="${textfile%%.$textextension}.$audioformat"
            if sox --no-show-progress <(curl --silent --get --data "$curl_data" --data-urlencode "INPUT_TEXT@$textfile.work" $maryserver/process) --type wav "$audiofile" 2>/dev/null; then
                echo "converted $textfile"
            else
                echo "failed $textfile" >&2
            fi
            mv "$textfile.work" "$textfile.done"
        fi
    done
}

curl $maryserver &>/dev/null || fail "marytts-server ($maryserver) not started"
cd "$1" || fail "folder '$1' not found or no access"

echo "`date` start"

for thread in `seq 1 $cores`; do
    txt2signal &
done

wait

echo "`date` end"
