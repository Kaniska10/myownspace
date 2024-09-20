#!/bin/bash

URL="https://api.github.com/zen"

while true; do
        response=$(curl --request GET --url "$URL"  \
                --header "Authorization: Bearer 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" \
                  --header "X-GitHub-Api-Version: 2022-11-28")
        echo "$response"
        sleep 2
done
