#!/bin/bash
# Start the java application in the background
java -Dcom.sun.jndi.ldap.object.trustURLCodebase=true \
     -Dlog4j2.formatMsgNoLookups=false \
     -jar app.jar &
JAVA_PID=$!

# Optionally, you can wait for the java process (if you want to restart it later, you might not want to wait)
# wait $JAVA_PID

# Keep the container alive indefinitely
tail -f /dev/null