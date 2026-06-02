#!/usr/bin/env bash
set -e

SRC="src/main/resources/hello.src"
OUT_DIR="demo_out"
PROGRAM="$OUT_DIR/program"
ASM="$OUT_DIR/program.asm"
OBJ="$OUT_DIR/program.o"
IR="$OUT_DIR/program.ir"
AST="$OUT_DIR/program.ast"

clear

echo "========================================"
echo " Демонстрация работы компилятора mycc"
echo "========================================"
echo

if [ ! -f "./mycc" ]; then
    echo "Ошибка: файл ./mycc не найден"
    exit 1
fi

if [ ! -f "$SRC" ]; then
    echo "Ошибка: исходный файл $SRC не найден"
    exit 1
fi

chmod +x ./mycc
mkdir -p "$OUT_DIR"

echo "1. Версия компилятора"
echo "----------------------------------------"
./mycc --version
echo


echo
echo "2. Справка по командной строке"
echo "----------------------------------------"
./mycc --help
echo


echo
echo "3. Исходная программа"
echo "----------------------------------------"
cat "$SRC"
echo


echo
echo "4. Вывод AST"
echo "----------------------------------------"
./mycc --ast "$SRC" > "$AST"
cat "$AST"
echo
echo "AST сохранено в $AST"
echo


echo
echo "5. Вывод IR"
echo "----------------------------------------"
./mycc --ir "$SRC" > "$IR"
cat "$IR"
echo
echo "IR сохранено в $IR"
echo


echo
echo "6. Генерация assembly-кода"
echo "----------------------------------------"
./mycc -S "$SRC" -o "$ASM" -v
echo
echo "Assembly сохранён в $ASM"
echo
echo "Первые строки assembly:"
head -n 30 "$ASM"
echo


echo
echo "7. Компиляция до object-файла"
echo "----------------------------------------"
./mycc -c "$SRC" -o "$OBJ" -v
echo
echo "Object-файл создан:"
ls -lh "$OBJ"
echo


echo
echo "8. Полная компиляция source -> executable"
echo "----------------------------------------"
./mycc "$SRC" -o "$PROGRAM" -v
echo
echo "Исполняемый файл создан:"
ls -lh "$PROGRAM"
echo


echo
echo "9. Запуск скомпилированной программы"
echo "----------------------------------------"
"$PROGRAM"
EXIT_CODE=$?
echo
echo "Exit code: $EXIT_CODE"
echo


echo
echo "10. Проверка обработки ошибок"
echo "----------------------------------------"

BAD_SRC="$OUT_DIR/broken.src"

cat > "$BAD_SRC" << 'EOF'
int main() {
    int x = 10
    return x;
}
EOF

echo "Файл с ошибкой:"
cat "$BAD_SRC"
echo

set +e
./mycc "$BAD_SRC" -o "$OUT_DIR/broken_program"
BAD_EXIT_CODE=$?
set -e

echo
echo "Код завершения компилятора: $BAD_EXIT_CODE"
echo


echo
echo "11. Проверка созданных файлов"
echo "----------------------------------------"
ls -lh "$OUT_DIR"
echo

echo "========================================"
echo " Демонстрация завершена"
echo "========================================"