#!/bin/bash
CHANNEL=$1
BUILD_STATUS=$2
SLACK_MSG=$3

if [ -z "$CHANNEL" ] || [ -z "$SLACK_MSG" ] || [ -z "$BUILD_STATUS" ] ||[ -z "$CI_SLACK_WEBHOOK_URL" ]; then
    echo "Missing argument(s) - Use: $0 channel message"
    echo "Missing build status argument - PASS | FAIL"
    echo "set CI_SLACK_WEBHOOK_URL environment variable."
else
	if [[ "${BUILD_STATUS}_" == "FAIL"_ ]];then
	curl -X POST --data-urlencode 'payload={"channel": "'"$CHANNEL"'", "username": "Wizni CI bot", "text": "'"$SLACK_MSG"'", "icon_emoji": ":sob:"}' "$CI_SLACK_WEBHOOK_URL"
	else
	curl -X POST --data-urlencode 'payload={"channel": "'"$CHANNEL"'", "username": "Wizni CI bot", "text": "'"$SLACK_MSG"'", "icon_emoji": ":thumbsup:"}' "$CI_SLACK_WEBHOOK_URL"
	fi

fi
