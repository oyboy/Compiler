bits 64
section .data
L_str_1 db "flag value:", 0
L_str_0 db "x value:", 0

section .text
global main
extern exit, printf, malloc, free, scanf, puts, getchar, pow, sqrt, sin, cos, strlen
extern print_int, print_bool, print_string, print_float

_start:
    call main
    mov rdi, rax
    call exit

main:
    push rbp
    mov rbp, rsp
    sub rsp, 64
.main_entry:
    mov qword [rbp - 8], 14
    mov rax, [rbp - 8]
    mov [rbp - 24], rax
    mov rax, [rbp - 24]
    mov r10, 1
    add rax, r10
    mov [rbp - 32], rax
    mov rax, [rbp - 32]
    mov [rbp - 8], rax
    jmp .main_or_true
.main_or_true:
    mov qword [rbp - 40], 1
    jmp .main_or_end
.main_or_end:
    mov rax, [rbp - 40]
    mov [rbp - 16], rax
    lea rdi, [L_str_0]
    call print_string
    mov rax, [rbp - 8]
    mov [rbp - 48], rax
    mov rdi, [rbp - 48]
    call print_int
    lea rdi, [L_str_1]
    call print_string
    mov rax, [rbp - 16]
    mov [rbp - 56], rax
    mov rdi, [rbp - 56]
    call print_bool
    mov rax, 0
    leave
    ret

