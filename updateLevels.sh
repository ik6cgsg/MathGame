#! /bin/bash
TYPE=$1
if [[ -z ${TYPE} ]]; then
    printf "Please, set type\n"
    exit
fi
ROOT=$(pwd)
ASSETS_DIR="${ROOT}/app/src/main/assets"
CLONE_DIR="${ROOT}/levels"
GAME_NAME="MathGame_IK_an"
# Clone new
git clone https://bitbucket.org/vkatsman/mathgameslevels.git ${CLONE_DIR}
if [[ ! -d "${CLONE_DIR}/${GAME_NAME}/${TYPE}" ]]; then
    printf "No such task type\n"
    rm -rf ${CLONE_DIR}
    exit
fi
cd "${CLONE_DIR}"
# Copy files
cp -rf "./${GAME_NAME}/${TYPE}/." "${ASSETS_DIR}"
# Clear old
cd "${ROOT}"
rm -rf ${CLONE_DIR}
