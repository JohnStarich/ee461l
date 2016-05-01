#!/usr/bin/env bash -e

function error {
    if [ -z "$@" ]; then
        cat - >&2
    else
        echo "$@" >&2
    fi
}

function validate_args {
    SCRIPT_NAME=$0
    SCRIPT_DIR=$(cd $(dirname ${BASH_SOURCE[0]}) && pwd)
    BASE_DIR=$(dirname ${SCRIPT_DIR})
    EMBER_DIR="$BASE_DIR/src/main/static"
    EMBER_OUTPUT_DIR="$BASE_DIR/target/classes/static"
    ENVIRONMENT=$1
    if [ -z "$ENVIRONMENT" ]; then
        error 'Environment must be set'
        return 2
    fi
}

function print_usage {
    error <<EOT
Usage: ember-build.sh {development,production}
EOT
}

function build {
    cd ${EMBER_DIR}
    ember build \
        --environment ${ENVIRONMENT} \
        --output-path ${EMBER_OUTPUT_DIR} \
        --watch
}

function install {
    cd ${EMBER_DIR}
    npm install && bower install
}

validate_args $@ || (print_usage && exit 2)

install && build
