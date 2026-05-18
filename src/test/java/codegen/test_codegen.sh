#!/bin/bash

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'
YELLOW='\033[1;33m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../../" && pwd)"

JAVA_BIN="java"
CLASSPATH="$PROJECT_ROOT/target/classes"
RUNTIME_SRC="$PROJECT_ROOT/src/main/java/runtime/runtime.asm"
TEMP_DIR="$SCRIPT_DIR/tests/temp"

mkdir -p "$TEMP_DIR"

echo -e "${YELLOW}=== X86-64 Codegen Test Suite ===${NC}"

if [ ! -d "$CLASSPATH" ]; then
    echo -e "${RED}Error: CLASSPATH not found: $CLASSPATH${NC}"
    exit 1
fi

if [ ! -f "$RUNTIME_SRC" ]; then
    echo -e "${RED}Error: runtime.asm not found: $RUNTIME_SRC${NC}"
    exit 1
fi

nasm -f elf64 "$RUNTIME_SRC" -o "$TEMP_DIR/runtime.o"
if [ $? -ne 0 ]; then
    echo -e "${RED}Error: Failed to assemble runtime.asm${NC}"
    exit 1
fi

PASSED=0
FAILED=0

run_tests() {
    local dir="$1"
    local abs_dir="$SCRIPT_DIR/$dir"

    if [ ! -d "$abs_dir" ]; then
        return
    fi

    echo -e "\n${YELLOW}Running tests in: $dir${NC}"

    for src_file in "$abs_dir"/*.src; do
        [ -e "$src_file" ] || continue

        test_name=$(basename "$src_file")
        expected_file="${src_file%.src}.expected"

        echo -n "Testing $test_name ... "

        $JAVA_BIN -cp "$CLASSPATH" com.scammers.Main "$src_file" \
            --asm --asm-output "$TEMP_DIR/output.asm"

        if [ $? -ne 0 ]; then
            echo -e "${RED}FAIL (Compiler Error)${NC}"
            ((FAILED++))
            continue
        fi

        nasm -f elf64 "$TEMP_DIR/output.asm" -o "$TEMP_DIR/output.o" > /dev/null 2>&1
        if [ $? -ne 0 ]; then
            echo -e "${RED}FAIL (NASM Syntax Error)${NC}"
            ((FAILED++))
            continue
        fi

        ld "$TEMP_DIR/output.o" "$TEMP_DIR/runtime.o" -o "$TEMP_DIR/test_exec"
        if [ $? -ne 0 ]; then
            echo -e "${RED}FAIL (Linker Error)${NC}"
            ((FAILED++))
            continue
        fi

        "$TEMP_DIR/test_exec"
        actual_stdout=$("$TEMP_DIR/test_exec")
        actual_exit_code=$?

        if [ -f "$expected_file" ]; then
                    expected_exit=$(grep "EXIT:" "$expected_file" | cut -d' ' -f2)
                    expected_stdout=$(grep "STDOUT:" "$expected_file" | cut -d' ' -f2)

                    SUCCESS=true
                    if [ "$actual_exit_code" -ne "$expected_exit" ]; then
                        echo -e "${RED}FAIL (Exit Code)${NC} Expected $expected_exit, got $actual_exit_code"
                        SUCCESS=false
                    fi

                    if [ ! -z "$expected_stdout" ] && [ "$actual_stdout" != "$expected_stdout" ]; then
                        echo -e "${RED}FAIL (STDOUT)${NC} Expected '$expected_stdout', got '$actual_stdout'"
                        SUCCESS=false
                    fi

                    if [ "$SUCCESS" = true ]; then
                        echo -e "${GREEN}OK${NC}"
                        ((PASSED++))
                    else
                        ((FAILED++))
                    fi
        else
            echo -e "${YELLOW}MISSING .expected file${NC}"
            ((FAILED++))
        fi
    done
}

run_tests "valid/arithmetic_ops"
run_tests "valid/control_flow"
run_tests "valid/function_calls"
run_tests "valid/integration"

echo -e "\n----------------------------------------"
echo -e "TOTAL: $((PASSED + FAILED))"
echo -e "PASSED: ${GREEN}$PASSED${NC}"
echo -e "FAILED: ${RED}$FAILED${NC}"

rm -f "$TEMP_DIR/output.asm" "$TEMP_DIR/output.o" "$TEMP_DIR/test_exec"

if [ $FAILED -gt 0 ]; then
    exit 1
fi