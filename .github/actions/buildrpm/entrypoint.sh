#!/bin/sh -l

echo "Building rpm based on source code located at $1"
time=$(date)
make rpm

source_rpm_path="rpmbuild/SRPMS"
rpm_path="rpmbuild/RPMS"

echo $(ls -r /github/home)

echo "::set-output name=source_rpm_path::$source_rpm_path"
echo "::set-output name=rpm_path::$rpm_path"