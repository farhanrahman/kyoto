#!/bin/sh

mvn exec:java -Dexec.mainClass="uk.ac.imperial.presage2.core.cli.Presage2CLI" -Dexec.args="$*" --quiet
