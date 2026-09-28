package com.patamar.app.core.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidatorTest {

    @Test fun `normaliza email com trim e minusculas`() {
        assertEquals("joao.silva@gmail.com", AuthValidator.normalizeEmail("  Joao.Silva@Gmail.COM \n"))
    }

    @Test fun `aceita emails validos`() {
        listOf(
            "a@b.co", "teste@patamar.app", "joao.silva+tag@empresa.com.br",
            "user_name@sub.dominio.org", "x-y@a-b.io", "123@456.com"
        ).forEach { assertNull("deveria aceitar $it", AuthValidator.validateEmail(it)) }
    }

    @Test fun `rejeita emails invalidos`() {
        listOf(
            "", "   ", "semarroba.com", "@dominio.com", "usuario@", "usuario@dominio",
            "usuario@dominio.c", "usuario@.com", "usuario@dominio..com", "usu..ario@dominio.com",
            ".usuario@dominio.com", "usuario.@dominio.com", "usu ario@dominio.com",
            "usuario@dominio.com.", "usuario@-dominio.com", "a@b@c.com", "usuário@dominio.com"
        ).forEach { assertNotNull("deveria rejeitar '$it'", AuthValidator.validateEmail(it)) }
    }

    @Test fun `rejeita email acima de 254 caracteres`() {
        val long = "a".repeat(60) + "@" + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(63) + ".com"
        assertEquals("E-mail muito longo", AuthValidator.validateEmail(long))
    }

    @Test fun `nome valido aceita acentos hifen e apostrofo`() {
        listOf("João da Silva", "Ana-Maria", "D'Ávila", "Zoë Müller", "Ana").forEach {
            assertNull("deveria aceitar $it", AuthValidator.validateName(it))
        }
    }

    @Test fun `nome invalido`() {
        assertEquals("Informe seu nome", AuthValidator.validateName("   "))
        assertEquals("Mínimo 3 caracteres", AuthValidator.validateName("Jo"))
        assertNotNull(AuthValidator.validateName("Joao123"))
        assertNotNull(AuthValidator.validateName("Joao_Silva"))
        assertNotNull(AuthValidator.validateName("-Joao"))
        assertNotNull(AuthValidator.validateName("<script>"))
        assertEquals("Máximo 60 caracteres", AuthValidator.validateName("a".repeat(61)))
    }

    @Test fun `normaliza espacos do nome`() {
        assertEquals("João da Silva", AuthValidator.normalizeName("  João   da \t Silva  "))
    }

    @Test fun `senha valida`() {
        assertNull(AuthValidator.validateNewPassword("Teste@123"))
        assertNull(AuthValidator.validateNewPassword("Abcdef12"))
        assertNull(AuthValidator.validateNewPassword("A1" + "b".repeat(62)))
    }

    @Test fun `senha invalida devolve o primeiro problema`() {
        assertEquals("Informe a senha", AuthValidator.validateNewPassword(""))
        assertEquals("Mínimo 8 caracteres", AuthValidator.validateNewPassword("Ab1"))
        assertEquals("Máximo 64 caracteres", AuthValidator.validateNewPassword("Aa1" + "x".repeat(62)))
        assertEquals("A senha não pode conter espaços", AuthValidator.validateNewPassword("Abcd 1234"))
        assertEquals("A senha não pode conter espaços", AuthValidator.validateNewPassword("Abcdef12 "))
        assertEquals("Inclua ao menos uma letra maiúscula", AuthValidator.validateNewPassword("abcdef12"))
        assertEquals("Inclua ao menos uma letra minúscula", AuthValidator.validateNewPassword("ABCDEF12"))
        assertEquals("Inclua ao menos um número", AuthValidator.validateNewPassword("Abcdefgh"))
    }

    @Test fun `confirmacao de senha`() {
        assertNull(AuthValidator.validateConfirmPassword("Abcdef12", "Abcdef12"))
        assertEquals("As senhas não coincidem", AuthValidator.validateConfirmPassword("Abcdef12", "abcdef12"))
        assertEquals("Confirme a senha", AuthValidator.validateConfirmPassword("Abcdef12", ""))
    }
}
