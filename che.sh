#!/bin/sh
# Copyright (c) 2012-2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Tyler Jewell - Initial Implementation
#

init_logging() {
  BLUE='\033[1;34m'
  GREEN='\033[0;32m'
  RED='\033[0;31m'
  NC='\033[0m'
}

error_exit() {
  echo  "---------------------------------------"
  error "!!!"
  error "!!! ${1}"
  error "!!!"
  echo  "---------------------------------------"
  exit 1
}

check_docker() {
  if ! docker ps > /dev/null 2>&1; then
    output=$(docker ps)
    error_exit "Error - Docker not installed properly: \n${output}"
  fi

  # Prep script by getting default image
  if [ "$(docker images -q alpine 2> /dev/null)" = "" ]; then
    info "ECLIPSE CHE: PULLING IMAGE alpine:latest"
    docker pull alpine > /dev/null 2>&1
  fi
}

init_global_variables() {

  CHE_LAUNCHER_IMAGE_NAME="codenvy/che-launcher"
  CHE_SERVER_IMAGE_NAME="codenvy/che-server"
  CHE_FILE_IMAGE_NAME="codenvy/che-file"
  CHE_MOUNT_IMAGE_NAME="codenvy/che-mount"
  CHE_TEST_IMAGE_NAME="codenvy/che-test"

  CHE_LAUNCHER_CONTAINER_NAME="che-launcher"
  CHE_SERVER_CONTAINER_NAME="che-server"
  CHE_FILE_CONTAINER_NAME="che-file"
  CHE_MOUNT_CONTAINER_NAME="che-mount"
  CHE_TEST_CONTAINER_NAME="che-test"

  # User configurable variables
  DEFAULT_CHE_VERSION="nightly"
  DEFAULT_CHE_CLI_ACTION="help"

  CHE_VERSION=${CHE_VERSION:-${DEFAULT_CHE_VERSION}}
  CHE_CLI_ACTION=${CHE_CLI_ACTION:-${DEFAULT_CHE_CLI_ACTION}}

  GLOBAL_NAME_MAP=$(docker info | grep "Name:" | cut -d" " -f2)
  GLOBAL_HOST_ARCH=$(docker version --format {{.Client}} | cut -d" " -f5)
  GLOBAL_UNAME=$(docker run --rm alpine sh -c "uname -r")
  GLOBAL_GET_DOCKER_HOST_IP=$(get_docker_host_ip)

  USAGE="
Usage: che [COMMAND]
           start                              Starts Che server
           stop                               Stops Che server
           restart                            Restart Che server
           update                             Pulls specific version, respecting CHE_VERSION
           mount <local-path> <ws-ssh-port>   Synchronize workspace to a local directory
           init                               Initialize directory with Che configuration
           up                                 Create workspace from source in current directory
           info [ --all                       Run all debugging tests
                  --server                    Run Che launcher and server debugging tests
                  --networking                Test connectivity between Che sub-systems
                  --cli                       Print CLI (this program)debugging info
                  --create [<url>]            Test creating a workspace and project in Che
                           [<user>] 
                           [<pass>] ]
"
}

usage () {
  printf "%s" "${USAGE}"
}

info() {
  printf  "${GREEN}INFO:${NC} %s\n" "${1}"
}

debug() {
  printf  "${BLUE}DEBUG:${NC} %s\n" "${1}"
}

error() {
  printf  "${RED}ERROR:${NC} %s\n" "${1}"
}

parse_command_line () {
  if [ $# -eq 0 ]; then 
    CHE_CLI_ACTION="help"
  else
    case $1 in
      start|stop|restart|update|info|init|up|mount|test|help|-h|--help)
        CHE_CLI_ACTION=$1
      ;;
      *)
        # unknown option
        error_exit "You passed an unknown command line option."
      ;;
    esac
  fi
}

docker_exec() {
  if is_boot2docker || is_docker_for_windows; then
    MSYS_NO_PATHCONV=1 docker.exe "$@"
  else
    "$(which docker)" "$@"
  fi
}

get_docker_host_ip() {
  case $(get_docker_install_type) in
   boot2docker)
     NETWORK_IF="eth1"
   ;;
   native)
     NETWORK_IF="docker0"
   ;;
   *)
     NETWORK_IF="eth0"
   ;;
  esac
  
  docker run --rm --net host \
            alpine sh -c \
            "ip a show ${NETWORK_IF}" | \
            grep 'inet ' | \
            cut -d/ -f1 | \
            awk '{ print $2}'
}

get_docker_install_type() {
  if is_boot2docker; then
    echo "boot2docker"
  elif is_docker_for_windows; then
    echo "docker4windows"
  elif is_docker_for_mac; then
    echo "docker4mac"
  else
    echo "native"
  fi
}

is_boot2docker() {
  if echo "$GLOBAL_UNAME" | grep -q "boot2docker"; then
    return 0
  else
    return 1
  fi
}

is_docker_for_mac() {
  if is_moby_vm && ! has_docker_for_windows_client; then
    return 0
  else
    return 1
  fi
}

is_docker_for_windows() {
  if is_moby_vm && has_docker_for_windows_client; then
    return 0
  else
    return 1
  fi
}

is_native() {
  if [ $(get_docker_install_type) = "native" ]; then
    return 0
  else
    return 1
  fi
}

is_moby_vm() {
  if echo "$GLOBAL_NAME_MAP" | grep -q "moby"; then
    return 0
  else
    return 1
  fi
}

has_docker_for_windows_client(){
  if [ "${GLOBAL_HOST_ARCH}" = "windows" ]; then
    return 0
  else
    return 1
  fi
}

get_full_path() {
  echo $(realpath $1)
}

convert_windows_to_posix() {
  echo "/"$(echo "$1" | sed 's/\\/\//g' | sed 's/://')
}

get_clean_path() {
  INPUT_PATH=$1
  # \some\path => /some/path
  OUTPUT_PATH=$(echo ${INPUT_PATH} | tr '\\' '/')
  # /somepath/ => /somepath
  OUTPUT_PATH=${OUTPUT_PATH%/}
  # /some//path => /some/path
  OUTPUT_PATH=$(echo ${OUTPUT_PATH} | tr -s '/')
  # "/some/path" => /some/path
  OUTPUT_PATH=${OUTPUT_PATH//\"}
  echo ${OUTPUT_PATH}
}

get_mount_path() {
  FULL_PATH=$(get_full_path $1)
  POSIX_PATH=$(convert_windows_to_posix $FULL_PATH)
  echo $(get_clean_path $POSIX_PATH)
}


has_docker_for_windows_ip() {
  if [ "${GLOBAL_GET_DOCKER_HOST_IP}" = "10.0.75.2" ]; then
    return 0
  else
    return 1
  fi
}

get_che_hostname() {
  INSTALL_TYPE=$(get_docker_install_type)
  if [ "${INSTALL_TYPE}" = "boot2docker" ]; then
    echo $GLOBAL_GET_DOCKER_HOST_IP
  else
    echo "localhost"
  fi
}

get_list_of_che_system_environment_variables() {
  # See: http://stackoverflow.com/questions/4128235/what-is-the-exact-meaning-of-ifs-n
  IFS=$'\n'
  DOCKER_ENV=$(mktemp docker-run-env.XXX)

  # First grab all known CHE_ variables
  CHE_VARIABLES=($(env | grep CHE_))
  for SINGLE_VARIABLE in "${CHE_VARIABLES[@]}"; do
    echo "${SINGLE_VARIABLE}" >> $DOCKER_ENV
  done

  # Add in known proxy variables
  if [ ! -z ${http_proxy+x} ]; then 
  	echo "http_proxy=${http_proxy}" >> $DOCKER_ENV
  fi

  if [ ! -z ${https_proxy+x} ]; then 
    echo "https_proxy=${https_proxy}" >> $DOCKER_ENV
  fi

  if [ ! -z ${no_proxy+x} ]; then 
    echo "no_proxy=${no_proxy}" >> $DOCKER_ENV
  fi

  echo $DOCKER_ENV
}

check_current_image_and_update_if_not_found() {

  CURRENT_IMAGE=$(docker images -q "$1":"${CHE_VERSION}")

  if [ "${CURRENT_IMAGE}" != "" ]; then
    info "ECLIPSE CHE: FOUND IMAGE $1:${CHE_VERSION}"
  else
    update_che_image $1
  fi
}

execute_che_launcher() {

  check_current_image_and_update_if_not_found ${CHE_LAUNCHER_IMAGE_NAME}

  info "ECLIPSE CHE: LAUNCHING LAUNCHER"

  ENV_FILE=$(get_list_of_che_system_environment_variables)

  docker_exec run -t --rm --name "${CHE_LAUNCHER_CONTAINER_NAME}" \
    -v /var/run/docker.sock:/var/run/docker.sock \
    --env-file=$ENV_FILE \
    "${CHE_LAUNCHER_IMAGE_NAME}":"${CHE_VERSION}" "${CHE_CLI_ACTION}" || true

  # Remove temporary file
  rm -rf $ENV_FILE
}

execute_che_file() {

  check_current_image_and_update_if_not_found ${CHE_FILE_IMAGE_NAME}
  info "ECLIPSE CHE FILE: LAUNCHING CONTAINER"

  CURRENT_DIRECTORY=$(get_mount_path "${PWD}")
  docker_exec run -it --rm --name "${CHE_FILE_CONTAINER_NAME}" \
         -v /var/run/docker.sock:/var/run/docker.sock \
         -v "$CURRENT_DIRECTORY":"$CURRENT_DIRECTORY" \
         "${CHE_FILE_IMAGE_NAME}":"${CHE_VERSION}" \
         "${CURRENT_DIRECTORY}" "${CHE_CLI_ACTION}"
    # > /dev/null 2>&1
}

update_che_image() {
  if [ -z "${CHE_VERSION}" ]; then
    CHE_VERSION=${DEFAULT_CHE_VERSION}
  fi

  info "ECLIPSE CHE: PULLING IMAGE $1:${CHE_VERSION}"
  info ""
  docker pull $1:${CHE_VERSION}
  info ""
  info "ECLIPSE CHE: IMAGE $1:${CHE_VERSION} INSTALLED"
}

mount_local_directory() {
  if [ ! $# -eq 3 ]; then 
    error "che mount: Wrong number of arguments provided."
    return
  fi

  MOUNT_PATH=$(get_mount_path $2)

  if [ ! -e "${MOUNT_PATH}" ]; then
    error "che mount: Path provided does not exist."
    return
  fi

  if [ ! -d "${MOUNT_PATH}" ]; then
    error "che mount: Path provided is not a valid directory."
    return
  fi

  docker_exec run --rm -it --cap-add SYS_ADMIN \
                  --device /dev/fuse \
                  --name "${CHE_MOUNT_CONTAINER_NAME}" \
                  -v "${MOUNT_PATH}":/mnthost \
                  "${CHE_MOUNT_IMAGE_NAME}":"${CHE_VERSION}" "${GLOBAL_GET_DOCKER_HOST_IP}" $3
}

execute_che_debug() {

  if [ $# -eq 1 ]; then
    TESTS="--server"
  else
    TESTS=$2
  fi
  
  case $TESTS in
    --all|-all)
      print_che_cli_debug
      execute_che_launcher
      run_connectivity_tests
      execute_che_test "$@"
    ;;
    --cli|-cli)
      print_che_cli_debug
    ;;
    --networking|-networking)
      run_connectivity_tests
    ;;
    --server|-server)
      print_che_cli_debug
      info ""
      execute_che_launcher
    ;;
    --create|-create)
      execute_che_test "$@"
    ;;
    *)
      debug "Unknown debug flag passed: $2. Exiting."
    ;;
  esac

}

execute_che_test() {

  docker_exec run --rm -it --name "${CHE_TEST_CONTAINER_NAME}" \
                  -v /var/run/docker.sock:/var/run/docker.sock \
                  "${CHE_TEST_IMAGE_NAME}":"${CHE_VERSION}" "$@"
}

print_che_cli_debug() {
  debug "---------------------------------------"
  debug "---------  CHE CLI DEBUG INFO  --------"
  debug "---------------------------------------"
  debug ""
  debug "---------  PLATFORM INFO  -------------"
  debug "DOCKER_INSTALL_TYPE       = $(get_docker_install_type)"
  debug "DOCKER_HOST_IP            = ${GLOBAL_GET_DOCKER_HOST_IP}"
  debug "IS_DOCKER_FOR_WINDOWS     = $(is_docker_for_windows && echo "YES" || echo "NO")"
  debug "IS_DOCKER_FOR_MAC         = $(is_docker_for_mac && echo "YES" || echo "NO")"
  debug "IS_BOOT2DOCKER            = $(is_boot2docker && echo "YES" || echo "NO")"
  debug "IS_NATIVE                 = $(is_native && echo "YES" || echo "NO")"
  debug "HAS_DOCKER_FOR_WINDOWS_IP = $(has_docker_for_windows_ip && echo "YES" || echo "NO")"
  debug "IS_MOBY_VM                = $(is_moby_vm && echo "YES" || echo "NO")"
  debug ""
  debug "---------------------------------------"
  debug "---------------------------------------"
  debug "---------------------------------------"
  # Clenaup from any previous lingering tests
}

run_connectivity_tests() {
  debug ""
  debug "---------------------------------------"
  debug "-------- CHE CONNECTIVITY TEST --------"
  debug "---------------------------------------"
  # Start a fake workspace agent
  docker_exec run -d -p 12345:80 --name fakeagent alpine httpd -f -p 80 -h /etc/ > /dev/null

  AGENT_INTERNAL_IP=$(docker inspect --format='{{.NetworkSettings.IPAddress}}' fakeagent)
  AGENT_INTERNAL_PORT=80
  AGENT_EXTERNAL_IP=$GLOBAL_GET_DOCKER_HOST_IP
  AGENT_EXTERNAL_PORT=12345


  ### TEST 1: Simulate browser ==> workspace agent HTTP connectivity
  HTTP_CODE=$(curl -I $(get_che_hostname):${AGENT_EXTERNAL_PORT}/alpine-release \
                          -s -o /dev/null --connect-timeout 5 \
                          --write-out "%{http_code}") || echo "28" > /dev/null

  if [ "${HTTP_CODE}" = "200" ]; then
      debug "Browser             => Workspace Agent (Hostname)   : Connection succeeded"
  else
      debug "Browser             => Workspace Agent (Hostname)   : Connection failed"
  fi

  ### TEST 1a: Simulate browser ==> workspace agent HTTP connectivity
  HTTP_CODE=$(curl -I ${AGENT_EXTERNAL_IP}:${AGENT_EXTERNAL_PORT}/alpine-release \
                          -s -o /dev/null --connect-timeout 5 \
                          --write-out "%{http_code}") || echo "28" > /dev/null

  if [ "${HTTP_CODE}" = "200" ]; then
      debug "Browser             => Workspace Agent (External IP): Connection succeeded"
  else
      debug "Browser             => Workspace Agent (External IP): Connection failed"
  fi

  ### TEST 2: Simulate Che server ==> workspace agent (external IP) connectivity 
  export HTTP_CODE=$(docker run --rm --name fakeserver \
                                --entrypoint=curl \
                                codenvy/che-server:nightly \
                                  -I ${AGENT_EXTERNAL_IP}:${AGENT_EXTERNAL_PORT}/alpine-release \
                                  -s -o /dev/null \
                                  --write-out "%{http_code}")
  
  if [ "${HTTP_CODE}" = "200" ]; then
      debug "Che Server          => Workspace Agent (External IP): Connection succeeded"
  else
      debug "Che Server          => Workspace Agent (External IP): Connection failed"
  fi

  ### TEST 3: Simulate Che server ==> workspace agent (internal IP) connectivity 
  export HTTP_CODE=$(docker run --rm --name fakeserver \
                                --entrypoint=curl \
                                codenvy/che-server:nightly \
                                  -I ${AGENT_EXTERNAL_IP}:${AGENT_EXTERNAL_PORT}/alpine-release \
                                  -s -o /dev/null \
                                  --write-out "%{http_code}")

  if [ "${HTTP_CODE}" = "200" ]; then
      debug "Che Server          => Workspace Agent (Internal IP): Connection succeeded"
  else
      debug "Che Server          => Workspace Agent (Internal IP): Connection failed"
  fi

  docker rm -f fakeagent > /dev/null
}

# See: https://sipb.mit.edu/doc/safe-shell/
set -e
set -u
init_logging
check_docker
init_global_variables
parse_command_line "$@"

if is_boot2docker; then
  debug "Boot2docker detected - limited mounting"
  debug "Host OS -> Che folder mapping disabled"
  debug "Consider Docker for Mac or Windows to activate mounting"
  debug ""
fi

case ${CHE_CLI_ACTION} in
  start|stop|restart)
    execute_che_launcher
  ;;
  init|up)
    execute_che_file
  ;;
  update)
    update_che_image ${CHE_LAUNCHER_IMAGE_NAME}
    update_che_image ${CHE_SERVER_IMAGE_NAME}
    update_che_image ${CHE_MOUNT_IMAGE_NAME}
    update_che_image ${CHE_FILE_IMAGE_NAME}
  ;;
  mount)
    mount_local_directory "$@"
  ;;
  info)
    execute_che_debug "$@"
  ;;
  help)
    usage
  ;;
esac
