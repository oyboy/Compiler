bits 64
section .data
L_str_2 db "Initial array", 0
L_str_3 db "Sorted array", 0
L_str_4 db "Factorial of 5", 0
L_str_5 db "Fibonacci of 10", 0
L_str_6 db "Enter a number: ", 0
L_str_7 db "%d", 0
L_str_8 db "You entered: %d", 10, "", 0
L_str_9 db "sin(x) = %f", 10, "", 0
L_str_10 db "cos(x) = %f", 10, "", 0
L_str_0 db "Math demo", 0
L_flt_0 dq 2.0
L_flt_3 dq 30.0
L_flt_2 dq 144.0
L_flt_1 dq 8.0

section .text
global main
extern exit, printf, malloc, free, scanf, puts, getchar, pow, sqrt, sin, cos, strlen
extern print_int, print_bool, print_string, print_float

_start:
    call main
    mov rdi, rax
    call exit

fib:
    push rbp
    mov rbp, rsp
    sub rsp, 128
    mov qword [rbp - 8], rdi
    mov qword [rbp - 16], rsi
.fib_entry:
    mov rax, [rbp - 8]
    cmp rax, 1
    setle al
    movzx rax, al
    mov [rbp - 32], rax
    mov rax, [rbp - 32]
    test rax, rax
    jnz .fib_L_true
    jmp .fib_L_endif
.fib_L_true:
    mov rax, [rbp - 8]
    leave
    ret
.fib_L_endif:
    mov r10, [rbp - 16]
    mov r11, [rbp - 8]
    shl r11, 3
    add r10, r11
    mov rax, [r10]
    mov [rbp - 40], rax
    mov rax, 1
    neg rax
    mov [rbp - 48], rax
    mov rax, [rbp - 40]
    cmp rax, [rbp - 48]
    setnz al
    movzx rax, al
    mov [rbp - 56], rax
    mov rax, [rbp - 56]
    test rax, rax
    jnz .fib_L_true_0
    jmp .fib_L_endif_1
.fib_L_true_0:
    mov r10, [rbp - 16]
    mov r11, [rbp - 8]
    shl r11, 3
    add r10, r11
    mov rax, [r10]
    mov [rbp - 64], rax
    mov rax, [rbp - 64]
    leave
    ret
.fib_L_endif_1:
    mov rax, [rbp - 8]
    mov r10, 1
    sub rax, r10
    mov [rbp - 72], rax
    mov rdi, [rbp - 72]
    mov rsi, [rbp - 16]
    call fib
    mov [rbp - 80], rax
    mov rax, [rbp - 8]
    mov r10, 2
    sub rax, r10
    mov [rbp - 88], rax
    mov rdi, [rbp - 88]
    mov rsi, [rbp - 16]
    call fib
    mov [rbp - 96], rax
    mov rax, [rbp - 80]
    mov r10, [rbp - 96]
    add rax, r10
    mov [rbp - 104], rax
    mov rax, [rbp - 104]
    mov [rbp - 24], rax
    mov rax, [rbp - 24]
    mov [rbp - 112], rax
    mov r10, [rbp - 16]
    mov r11, [rbp - 8]
    shl r11, 3
    add r10, r11
    mov rax, [rbp - 112]
    mov [r10], rax
    mov rax, [rbp - 24]
    mov [rbp - 120], rax
    mov rax, [rbp - 120]
    leave
    ret

factorial:
    push rbp
    mov rbp, rsp
    sub rsp, 48
    mov qword [rbp - 8], rdi
.factorial_entry:
    mov rax, [rbp - 8]
    cmp rax, 1
    setle al
    movzx rax, al
    mov [rbp - 16], rax
    mov rax, [rbp - 16]
    test rax, rax
    jnz .factorial_L_true
    jmp .factorial_L_endif
.factorial_L_true:
    mov rax, 1
    leave
    ret
.factorial_L_endif:
    mov rax, [rbp - 8]
    mov r10, 1
    sub rax, r10
    mov [rbp - 24], rax
    mov rdi, [rbp - 24]
    call factorial
    mov [rbp - 32], rax
    mov rax, [rbp - 8]
    mov r10, [rbp - 32]
    imul rax, r10
    mov [rbp - 40], rax
    mov rax, [rbp - 40]
    leave
    ret

bubble_sort:
    push rbp
    mov rbp, rsp
    sub rsp, 240
    mov qword [rbp - 8], rdi
    mov qword [rbp - 16], rsi
.bubble_sort_entry:
    mov qword [rbp - 24], 0
    jmp .bubble_sort_L_for
.bubble_sort_L_for:
    mov rax, [rbp - 24]
    mov [rbp - 48], rax
    mov rax, [rbp - 48]
    cmp rax, [rbp - 16]
    setl al
    movzx rax, al
    mov [rbp - 56], rax
    mov rax, [rbp - 56]
    test rax, rax
    jnz .bubble_sort_L_forbody
    jmp .bubble_sort_L_endfor
.bubble_sort_L_forbody:
    mov qword [rbp - 32], 0
    jmp .bubble_sort_L_for_0
.bubble_sort_L_for_0:
    mov rax, [rbp - 32]
    mov [rbp - 64], rax
    mov rax, [rbp - 16]
    mov r10, 1
    sub rax, r10
    mov [rbp - 72], rax
    mov rax, [rbp - 64]
    cmp rax, [rbp - 72]
    setl al
    movzx rax, al
    mov [rbp - 80], rax
    mov rax, [rbp - 80]
    test rax, rax
    jnz .bubble_sort_L_forbody_1
    jmp .bubble_sort_L_endfor_3
.bubble_sort_L_forbody_1:
    mov rax, [rbp - 32]
    mov [rbp - 88], rax
    mov r10, [rbp - 8]
    mov r11, [rbp - 88]
    shl r11, 3
    add r10, r11
    mov rax, [r10]
    mov [rbp - 96], rax
    mov rax, [rbp - 32]
    mov [rbp - 104], rax
    mov rax, [rbp - 104]
    mov r10, 1
    add rax, r10
    mov [rbp - 112], rax
    mov r10, [rbp - 8]
    mov r11, [rbp - 112]
    shl r11, 3
    add r10, r11
    mov rax, [r10]
    mov [rbp - 120], rax
    mov rax, [rbp - 96]
    cmp rax, [rbp - 120]
    setg al
    movzx rax, al
    mov [rbp - 128], rax
    mov rax, [rbp - 128]
    test rax, rax
    jnz .bubble_sort_L_true
    jmp .bubble_sort_L_endif
.bubble_sort_L_true:
    mov rax, [rbp - 32]
    mov [rbp - 136], rax
    mov r10, [rbp - 8]
    mov r11, [rbp - 136]
    shl r11, 3
    add r10, r11
    mov rax, [r10]
    mov [rbp - 144], rax
    mov rax, [rbp - 144]
    mov [rbp - 40], rax
    mov rax, [rbp - 32]
    mov [rbp - 152], rax
    mov rax, [rbp - 152]
    mov r10, 1
    add rax, r10
    mov [rbp - 160], rax
    mov r10, [rbp - 8]
    mov r11, [rbp - 160]
    shl r11, 3
    add r10, r11
    mov rax, [r10]
    mov [rbp - 168], rax
    mov rax, [rbp - 32]
    mov [rbp - 176], rax
    mov r10, [rbp - 8]
    mov r11, [rbp - 176]
    shl r11, 3
    add r10, r11
    mov rax, [rbp - 168]
    mov [r10], rax
    mov rax, [rbp - 40]
    mov [rbp - 184], rax
    mov rax, [rbp - 32]
    mov [rbp - 192], rax
    mov rax, [rbp - 192]
    mov r10, 1
    add rax, r10
    mov [rbp - 200], rax
    mov r10, [rbp - 8]
    mov r11, [rbp - 200]
    shl r11, 3
    add r10, r11
    mov rax, [rbp - 184]
    mov [r10], rax
    jmp .bubble_sort_L_endif
.bubble_sort_L_endif:
    mov rax, [rbp - 32]
    mov [rbp - 208], rax
    mov rax, [rbp - 208]
    mov r10, 1
    add rax, r10
    mov [rbp - 216], rax
    mov rax, [rbp - 216]
    mov [rbp - 32], rax
    jmp .bubble_sort_L_for_0
.bubble_sort_L_endfor_3:
    mov rax, [rbp - 24]
    mov [rbp - 224], rax
    mov rax, [rbp - 224]
    mov r10, 1
    add rax, r10
    mov [rbp - 232], rax
    mov rax, [rbp - 232]
    mov [rbp - 24], rax
    jmp .bubble_sort_L_for
.bubble_sort_L_endfor:
    leave
    ret

print_array:
    push rbp
    mov rbp, rsp
    sub rsp, 80
    mov qword [rbp - 8], rdi
    mov qword [rbp - 16], rsi
.print_array_entry:
    mov qword [rbp - 24], 0
    jmp .print_array_L_for
.print_array_L_for:
    mov rax, [rbp - 24]
    mov [rbp - 32], rax
    mov rax, [rbp - 32]
    cmp rax, [rbp - 16]
    setl al
    movzx rax, al
    mov [rbp - 40], rax
    mov rax, [rbp - 40]
    test rax, rax
    jnz .print_array_L_forbody
    jmp .print_array_L_endfor
.print_array_L_forbody:
    mov rax, [rbp - 24]
    mov [rbp - 48], rax
    mov r10, [rbp - 8]
    mov r11, [rbp - 48]
    shl r11, 3
    add r10, r11
    mov rax, [r10]
    mov [rbp - 56], rax
    mov rdi, [rbp - 56]
    call print_int
    mov rax, [rbp - 24]
    mov [rbp - 64], rax
    mov rax, [rbp - 64]
    mov r10, 1
    add rax, r10
    mov [rbp - 72], rax
    mov rax, [rbp - 72]
    mov [rbp - 24], rax
    jmp .print_array_L_for
.print_array_L_endfor:
    leave
    ret

math_demo:
    push rbp
    mov rbp, rsp
    sub rsp, 32
.math_demo_entry:
    lea rdi, [L_str_0]
    call print_string
    movsd xmm0, [L_flt_0]
    movsd xmm1, [L_flt_1]
    call pow
    movsd [rbp - 8], xmm0
    movsd xmm0, [rbp - 8]
    call print_float
    movsd xmm0, [L_flt_2]
    call sqrt
    movsd [rbp - 16], xmm0
    movsd xmm0, [rbp - 16]
    call print_float
    movsd xmm0, [L_flt_3]
    call sin
    movsd [rbp - 24], xmm0
    movsd xmm0, [rbp - 24]
    call print_float
    movsd xmm0, [L_flt_3]
    call cos
    movsd [rbp - 32], xmm0
    movsd xmm0, [rbp - 32]
    call print_float
    jmp .math_demo_L_endif
.math_demo_L_endif:
    leave
    ret

benchmark_demo:
    push rbp
    mov rbp, rsp
    sub rsp, 80
.benchmark_demo_entry:
    mov qword [rbp - 8], 0
    mov qword [rbp - 16], 0
    jmp .benchmark_demo_L_for
.benchmark_demo_L_for:
    mov rax, [rbp - 16]
    mov [rbp - 24], rax
    mov rax, [rbp - 24]
    cmp rax, 1000
    setl al
    movzx rax, al
    mov [rbp - 32], rax
    mov rax, [rbp - 32]
    test rax, rax
    jnz .benchmark_demo_L_forbody
    jmp .benchmark_demo_L_endfor
.benchmark_demo_L_forbody:
    mov rax, [rbp - 8]
    mov [rbp - 40], rax
    mov rax, [rbp - 16]
    mov [rbp - 48], rax
    mov rax, [rbp - 40]
    mov r10, [rbp - 48]
    add rax, r10
    mov [rbp - 56], rax
    mov rax, [rbp - 56]
    mov [rbp - 8], rax
    mov rax, [rbp - 16]
    mov [rbp - 64], rax
    mov rax, [rbp - 64]
    mov r10, 1
    add rax, r10
    mov [rbp - 72], rax
    mov rax, [rbp - 72]
    mov [rbp - 16], rax
    jmp .benchmark_demo_L_for
.benchmark_demo_L_endfor:
    mov rax, [rbp - 8]
    mov [rbp - 80], rax
    mov rdi, [rbp - 80]
    call print_int
    leave
    ret

main:
    push rbp
    mov rbp, rsp
    sub rsp, 240
.main_entry:
    lea rdi, [L_str_2]
    call print_string
    mov rdi, 80
    xor rax, rax
    call malloc
    mov [rbp - 40], rax
    mov rax, [rbp - 40]
    mov [rbp - 8], rax
    mov r10, [rbp - 8]
    mov r11, 0
    shl r11, 3
    add r10, r11
    mov rax, 50
    mov [r10], rax
    mov r10, [rbp - 8]
    mov r11, 1
    shl r11, 3
    add r10, r11
    mov rax, 20
    mov [r10], rax
    mov r10, [rbp - 8]
    mov r11, 2
    shl r11, 3
    add r10, r11
    mov rax, 40
    mov [r10], rax
    mov r10, [rbp - 8]
    mov r11, 3
    shl r11, 3
    add r10, r11
    mov rax, 10
    mov [r10], rax
    mov r10, [rbp - 8]
    mov r11, 4
    shl r11, 3
    add r10, r11
    mov rax, 30
    mov [r10], rax
    mov r10, [rbp - 8]
    mov r11, 5
    shl r11, 3
    add r10, r11
    mov rax, 90
    mov [r10], rax
    mov r10, [rbp - 8]
    mov r11, 6
    shl r11, 3
    add r10, r11
    mov rax, 70
    mov [r10], rax
    mov r10, [rbp - 8]
    mov r11, 7
    shl r11, 3
    add r10, r11
    mov rax, 60
    mov [r10], rax
    mov r10, [rbp - 8]
    mov r11, 8
    shl r11, 3
    add r10, r11
    mov rax, 80
    mov [r10], rax
    mov r10, [rbp - 8]
    mov r11, 9
    shl r11, 3
    add r10, r11
    mov rax, 0
    mov [r10], rax
    mov rax, [rbp - 8]
    mov [rbp - 48], rax
    mov rdi, [rbp - 48]
    mov rsi, 10
    call print_array
    mov rax, [rbp - 8]
    mov [rbp - 56], rax
    mov rdi, [rbp - 56]
    mov rsi, 10
    call bubble_sort
    lea rdi, [L_str_3]
    call print_string
    mov rax, [rbp - 8]
    mov [rbp - 64], rax
    mov rdi, [rbp - 64]
    mov rsi, 10
    call print_array
    lea rdi, [L_str_4]
    call print_string
    mov rdi, 5
    call factorial
    mov [rbp - 72], rax
    mov rdi, [rbp - 72]
    call print_int
    mov rdi, 800
    xor rax, rax
    call malloc
    mov [rbp - 80], rax
    mov rax, [rbp - 80]
    mov [rbp - 16], rax
    mov qword [rbp - 24], 0
    jmp .main_L_for
.main_L_for:
    mov rax, [rbp - 24]
    mov [rbp - 88], rax
    mov rax, [rbp - 88]
    cmp rax, 100
    setl al
    movzx rax, al
    mov [rbp - 96], rax
    mov rax, [rbp - 96]
    test rax, rax
    jnz .main_L_forbody
    jmp .main_L_endfor
.main_L_forbody:
    mov rax, 1
    neg rax
    mov [rbp - 104], rax
    mov rax, [rbp - 24]
    mov [rbp - 112], rax
    mov r10, [rbp - 16]
    mov r11, [rbp - 112]
    shl r11, 3
    add r10, r11
    mov rax, [rbp - 104]
    mov [r10], rax
    mov rax, [rbp - 24]
    mov [rbp - 120], rax
    mov rax, [rbp - 120]
    mov r10, 1
    add rax, r10
    mov [rbp - 128], rax
    mov rax, [rbp - 128]
    mov [rbp - 24], rax
    jmp .main_L_for
.main_L_endfor:
    lea rdi, [L_str_5]
    call print_string
    mov rax, [rbp - 16]
    mov [rbp - 136], rax
    mov rdi, 10
    mov rsi, [rbp - 136]
    call fib
    mov [rbp - 144], rax
    mov rdi, [rbp - 144]
    call print_int
    lea rdi, [L_str_6]
    mov rax, 0
    call printf
    mov [rbp - 152], rax
    lea rdi, [L_str_7]
    lea rsi, [rbp - 32]
    xor rax, rax
    call scanf
    mov [rbp - 160], rax
    mov rax, [rbp - 32]
    mov [rbp - 168], rax
    lea rdi, [L_str_8]
    mov rsi, [rbp - 168]
    mov rax, 0
    call printf
    mov [rbp - 176], rax
    mov rax, [rbp - 32]
    mov [rbp - 184], rax
    cvtsi2sd xmm0, [rbp - 184]
    call sin
    movsd [rbp - 192], xmm0
    lea rdi, [L_str_9]
    movsd xmm0, [rbp - 192]
    mov rax, 1
    call printf
    mov [rbp - 200], rax
    mov rax, [rbp - 32]
    mov [rbp - 208], rax
    cvtsi2sd xmm0, [rbp - 208]
    call cos
    movsd [rbp - 216], xmm0
    lea rdi, [L_str_10]
    movsd xmm0, [rbp - 216]
    mov rax, 1
    call printf
    mov [rbp - 224], rax
    call math_demo
    call benchmark_demo
    call test
    mov rax, [rbp - 8]
    mov [rbp - 232], rax
    mov rdi, [rbp - 232]
    call free
    mov rax, [rbp - 16]
    mov [rbp - 240], rax
    mov rdi, [rbp - 240]
    call free
    mov rax, 0
    leave
    ret

test:
    push rbp
    mov rbp, rsp
    sub rsp, 128
.test_entry:
    mov qword [rbp - 8], 10
    mov rax, [rbp - 8]
    mov [rbp - 32], rax
    mov rax, [rbp - 32]
    mov r10, 8
    imul rax, r10
    mov [rbp - 40], rax
    mov rdi, [rbp - 40]
    xor rax, rax
    call malloc
    mov [rbp - 48], rax
    mov rax, [rbp - 48]
    mov [rbp - 16], rax
    mov qword [rbp - 24], 0
    jmp .test_L_loop
.test_L_loop:
    mov rax, [rbp - 24]
    mov [rbp - 56], rax
    mov rax, [rbp - 8]
    mov [rbp - 64], rax
    mov rax, [rbp - 56]
    cmp rax, [rbp - 64]
    setl al
    movzx rax, al
    mov [rbp - 72], rax
    mov rax, [rbp - 72]
    test rax, rax
    jnz .test_L_body
    jmp .test_L_endloop
.test_L_body:
    mov rax, [rbp - 24]
    mov [rbp - 80], rax
    mov rax, [rbp - 80]
    mov r10, 2
    imul rax, r10
    mov [rbp - 88], rax
    mov rax, [rbp - 24]
    mov [rbp - 96], rax
    mov r10, [rbp - 16]
    mov r11, [rbp - 96]
    shl r11, 3
    add r10, r11
    mov rax, [rbp - 88]
    mov [r10], rax
    mov rax, [rbp - 24]
    mov [rbp - 104], rax
    mov rax, [rbp - 104]
    mov r10, 1
    add rax, r10
    mov [rbp - 112], rax
    mov rax, [rbp - 112]
    mov [rbp - 24], rax
    jmp .test_L_loop
.test_L_endloop:
    mov rax, [rbp - 16]
    mov [rbp - 120], rax
    mov rdi, [rbp - 120]
    call free
    leave
    ret

