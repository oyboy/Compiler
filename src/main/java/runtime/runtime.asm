bits 64

section .data
    S1 db "true", 0
    S2 db "false", 0
    S3 db "-"
    S4 db "."
    S5 db 10
    minus_char db "-", 0

section .text
    global exit_program, print_int, print_bool, print_string, print_float

exit_program:
    mov rax, 60
    syscall

print_string:
    push rbp
    mov rbp, rsp
    mov rsi, rdi
    xor rdx, rdx
.L_s1:
    cmp byte [rsi + rdx], 0
    je .L_s2
    inc rdx
    jmp .L_s1
.L_s2:
    mov rax, 1
    mov rdi, 1
    syscall
    mov rax, 1
    mov rdi, 1
    mov rsi, S5
    mov rdx, 1
    syscall
    mov rsp, rbp
    pop rbp
    ret

print_bool:
    test rdi, rdi
    jz .L_b1
    mov rdi, S1
    jmp print_string
.L_b1:
    mov rdi, S2
    jmp print_string

print_int:
    push rbp
    mov rbp, rsp
    sub rsp, 32
    cmp rdi, 0
    jge .L_i1
    neg rdi
    mov r10, rdi
    mov rax, 1
    mov rdi, 1
    mov rsi, S3
    mov rdx, 1
    syscall
    mov rdi, r10
.L_i1:
    mov rax, rdi
    mov rcx, 10
    lea rsi, [rbp-1]
.L_i2:
    xor rdx, rdx
    div rcx
    add dl, '0'
    dec rsi
    mov [rsi], dl
    test rax, rax
    jnz .L_i2
    mov rdx, rbp
    sub rdx, rsi
    mov rax, 1
    mov rdi, 1
    syscall
    mov rax, 1
    mov rdi, 1
    mov rsi, S5
    mov rdx, 1
    syscall
    mov rsp, rbp
    pop rbp
    ret

print_float:
    push rbp
    mov rbp, rsp
    sub rsp, 16
    movsd [rbp-8], xmm0
    xorpd xmm1, xmm1
    ucomisd xmm0, xmm1
    jae .positive
    mov rax, 1
    mov rdi, 1
    mov rsi, minus_char
    mov rdx, 1
    syscall
    mov rax, 0x8000000000000000
    movq xmm1, rax
    xorpd xmm0, xmm1

.positive:
    cvttsd2si rdi, xmm0
    call L_P_RAW
    mov rax, 1
    mov rdi, 1
    mov rsi, S4
    mov rdx, 1
    syscall
    cvttsd2si rax, xmm0
    cvtsi2sd xmm1, rax
    subsd xmm0, xmm1
    mov rax, 1000000
    cvtsi2sd xmm1, rax
    mulsd xmm0, xmm1
    cvttsd2si rdi, xmm0
    call L_P_RAW
    mov rax, 1
    mov rdi, 1
    mov rsi, S5
    mov rdx, 1
    syscall

    leave
    ret

L_P_RAW:
    push rbp
    mov rbp, rsp
    sub rsp, 32
    mov rax, rdi
    mov rcx, 10
    lea rsi, [rbp-1]
    test rax, rax
    jnz .L_r1
    mov byte [rsi], '0'
    mov rdx, 1
    jmp .L_r2
.L_r1:
    xor rdx, rdx
    div rcx
    add dl, '0'
    dec rsi
    mov [rsi], dl
    test rax, rax
    jnz .L_r1
    lea rdx, [rbp-1]
    sub rdx, rsi
.L_r2:
    mov rax, 1
    mov rdi, 1
    syscall
    mov rsp, rbp
    pop rbp
    ret