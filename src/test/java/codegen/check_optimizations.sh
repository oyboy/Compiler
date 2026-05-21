#!/bin/bash

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'
YELLOW='\033[1;33m'

TEMP_DIR="/tmp/compiler_tests"
mkdir -p $TEMP_DIR

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../../" && pwd)"
JAVA_BIN="java"
CLASSPATH="$PROJECT_ROOT/target/classes"

echo -e "${YELLOW}=== Optimization Verification (ASM Analysis) ===${NC}"

check_opt() {
    local src=$1
    local forbidden_regex=$2
    local test_name=$(basename "$src")

    echo -n "Verifying $test_name ... "

    rm -f "$TEMP_DIR/opt.asm"

    $JAVA_BIN -cp "$CLASSPATH" com.scammers.Main "$src" \
        --asm --asm-output "$TEMP_DIR/opt.asm" > "$TEMP_DIR/error.log" 2>&1

    if [ $? -ne 0 ]; then
        echo -e "${RED}COMPILER ERROR${NC}"
        cat "$TEMP_DIR/error.log"
        return
    fi

    if [ ! -f "$TEMP_DIR/opt.asm" ]; then
        echo -e "${RED}FILE NOT CREATED${NC}"
        return
    fi

    found=$(grep -E "$forbidden_regex" "$TEMP_DIR/opt.asm")

    if [ -z "$found" ]; then
        echo -e "${GREEN}PASS${NC}"
    else
        echo -e "${RED}FAIL (Unoptimized)${NC}"
        echo "      Found: $(echo $found | xargs)"
    fi
}

check_opt "$SCRIPT_DIR/optimizations/test_const_fold.src" "add rax|imul rax"
check_opt "$SCRIPT_DIR/optimizations/test_dead_if.src" "call print_string|.main_L_true:"
check_opt "$SCRIPT_DIR/optimizations/test_dead_func.src" "never_called:"
check_opt "$SCRIPT_DIR/optimizations/test_dead_after_return.src" "100|add rax"