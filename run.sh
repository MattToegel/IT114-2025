#!/bin/bash
# Convert input to lowercase
input=$(echo "${2:-ui}" | tr '[:upper:]' '[:lower:]')
port=${3:-3000}  # Default port to 3000 if not 
# Default debug mode to false
debug=false
debugArg=""
# Check for -d flag
if [[ " $@ " =~ " -d " ]]; then
    debug=true
fi
if $debug; then
    debugArg="-agentlib:jdwp=transport=dt_socket,server=y,address=5005"
    echo "Debug mode is ON"
fi
project="${1%/}" # remove trailing slash
if [ "$input" = "server" ]; then
    java $debugArg "$project.Server.Server" "$port"
elif [ "$input" = "client" ]; then
    java $debugArg "$project.Client.Client"
elif [ "$input" = "ui" ]; then
    java $debugArg "$project.Client.ClientUI"
else
    echo "Must specify client or server or ui"
fi