#!/usr/bin/bash
cd || exit

if [ "$#" -lt 2 ]; then
    echo "Not enough arguments provided. Usage: $0 <repo_url> <repo_name>"
    exit 1
fi

git clone "$1" "$2"

cd "$2" || exit

pwd
