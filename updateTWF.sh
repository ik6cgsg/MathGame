#! /bin/bash
ROOT=$(pwd)
TWF_DIR="${ROOT}/twf"
CLONE_DIR="${ROOT}/newtwf"
MAIN="src/main/java"
TEST="src/test/java"
TWF_ADDR="com/twf"
# Clear old
cd "${TWF_DIR}/${MAIN}/${TWF_ADDR}"
rm -f *
cd "${TWF_DIR}/${TEST}"
rm -f *
mkdir "${TWF_ADDR}"
# Clone new
git clone https://bitbucket.org/vkatsman/twf.git ${CLONE_DIR}
cd "${CLONE_DIR}"
# Copy files
cp -rf "${MAIN}" "${TWF_DIR}/${MAIN}/${TWF_ADDR}"
cp -rf "${TEST}" "${TWF_DIR}/${TEST}/${TWF_ADDR}"
# Clear old
# rm -r ${CLONE_DIR}
cd "${TWF_DIR}/src"
sed -i "s/package /package com.twf./g" **/*.kt