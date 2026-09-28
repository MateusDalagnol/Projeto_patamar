package com.patamar.app.data.local.mock

import com.patamar.app.core.security.PasswordHasher
import com.patamar.app.core.utils.Constants
import com.patamar.app.data.model.Event
import com.patamar.app.data.model.EventCategory
import com.patamar.app.data.model.User
import java.time.LocalDate
import java.time.LocalTime

// BETA: dados fixos de Curitiba para popular o banco na primeira execução.
object MockDataSource {

    private val curated = listOf(
        Event(
            "evt_001", "Festival Vira-Lata", EventCategory.SHOW,
            "O maior festival de música independente da cidade.",
            lat = -25.4294, lng = -49.2714,
            address = "Largo da Ordem, Centro, Curitiba",
            date = LocalDate.now(), time = LocalTime.of(20, 0),
            distanceMeters = 1200, isFree = false, isHighlighted = true,
            imageUrl = coverUrl("evt_001")
        ),
        Event(
            "evt_002", "Feira do Largo", EventCategory.FEIRA,
            "Artesanato, gastronomia e cultura local toda semana.",
            lat = -25.4130, lng = -49.2700,
            address = "Largo da Ordem, Curitiba",
            date = LocalDate.now(), time = LocalTime.of(9, 0),
            distanceMeters = 800, isFree = true, isHighlighted = false,
            imageUrl = coverUrl("evt_002")
        ),
        Event(
            "evt_003", "Noite de Stand-up", EventCategory.TEATRO,
            "Os melhores comediantes locais em uma noite imperdível.",
            lat = -25.4310, lng = -49.2600,
            address = "Teatro do Paiol, Curitiba",
            date = LocalDate.now().plusDays(1), time = LocalTime.of(21, 0),
            distanceMeters = 2300, isFree = false, isHighlighted = true,
            imageUrl = coverUrl("evt_003")
        ),
        Event(
            "evt_004", "Exposição Urbana", EventCategory.ARTE,
            "Arte urbana e fotografia de rua de artistas curitibanos.",
            lat = -25.4200, lng = -49.2680,
            address = "Museu Oscar Niemeyer, Curitiba",
            date = LocalDate.now(), time = LocalTime.of(10, 0),
            distanceMeters = 3500, isFree = true, isHighlighted = false,
            imageUrl = coverUrl("evt_004")
        ),
        Event(
            "evt_005", "Corrida Noturna 5K", EventCategory.ESPORTES,
            "Percurso pelo centro histórico com largada às 19h.",
            lat = -25.4270, lng = -49.2630,
            address = "Praça Tiradentes, Curitiba",
            date = LocalDate.now().plusDays(3), time = LocalTime.of(19, 0),
            distanceMeters = 900, isFree = false, isHighlighted = false,
            imageUrl = coverUrl("evt_005")
        ),
        Event(
            "evt_006", "Food Truck Park", EventCategory.GASTRONOMIA,
            "20 food trucks no Parque Barigui com música ao vivo.",
            lat = -25.4050, lng = -49.3020,
            address = "Parque Barigui, Curitiba",
            date = LocalDate.now().plusDays(2), time = LocalTime.of(12, 0),
            distanceMeters = 6800, isFree = true, isHighlighted = true,
            imageUrl = coverUrl("evt_006")
        ),
        Event(
            "evt_007", "Club Noite Eletrônica", EventCategory.FESTA,
            "Set aberto com DJs locais e internacionais. 18+.",
            lat = -25.4350, lng = -49.2540,
            address = "Ópera de Arame, Curitiba",
            date = LocalDate.now(), time = LocalTime.of(23, 0),
            distanceMeters = 4100, isFree = false, isHighlighted = false,
            imageUrl = coverUrl("evt_007")
        ),
        Event(
            "evt_008", "Workshop de Cerâmica", EventCategory.ARTE,
            "Aula prática de 2 horas com material incluso.",
            lat = -25.4180, lng = -49.2750,
            address = "Centro Cultural Batel, Curitiba",
            date = LocalDate.now().plusDays(4), time = LocalTime.of(14, 0),
            distanceMeters = 2900, isFree = false, isHighlighted = false,
            imageUrl = coverUrl("evt_008")
        ),
        Event(
            "evt_009", "Feirinha Vegana", EventCategory.FEIRA,
            "Produtos naturais, cosméticos e gastronomia plant-based.",
            lat = -25.4240, lng = -49.2580,
            address = "Rua XV de Novembro, Curitiba",
            date = LocalDate.now().plusDays(5), time = LocalTime.of(8, 0),
            distanceMeters = 1600, isFree = true, isHighlighted = false,
            imageUrl = coverUrl("evt_009")
        ),
        Event(
            "evt_010", "Jam Session Jazz", EventCategory.SHOW,
            "Jam session aberta toda quinta-feira no subsolo.",
            lat = -25.4330, lng = -49.2700,
            address = "Bar do Alemão, Curitiba",
            date = LocalDate.now(), time = LocalTime.of(21, 30),
            distanceMeters = 500, isFree = false, isHighlighted = false,
            imageUrl = coverUrl("evt_010")
        ),
        Event(
            "evt_011", "Cinema Ao Ar Livre", EventCategory.ARTE,
            "Sessão gratuita de cinema no Passeio Público.",
            lat = -25.4190, lng = -49.2670,
            address = "Passeio Público, Curitiba",
            date = LocalDate.now().plusDays(6), time = LocalTime.of(20, 0),
            distanceMeters = 2100, isFree = true, isHighlighted = true,
            imageUrl = coverUrl("evt_011")
        ),
        Event(
            "evt_012", "Torneio de Xadrez", EventCategory.ESPORTES,
            "Open para amadores e profissionais. Inscrições na entrada.",
            lat = -25.4260, lng = -49.2650,
            address = "Biblioteca Pública, Curitiba",
            date = LocalDate.now().plusDays(7), time = LocalTime.of(9, 0),
            distanceMeters = 1800, isFree = true, isHighlighted = false,
            imageUrl = coverUrl("evt_012")
        )
    )

    // 50 eventos extras pra dar variedade (todas as categorias, datas nas próximas 3 semanas,
    // locais reais de Curitiba). A distância vem calculada a partir do centro da cidade.
    private val more = listOf(
        // ---- Shows ----
        ev(13, "Noite de Blues", EventCategory.SHOW, "Quarteto local revisita clássicos do blues com participações especiais.",
            -25.4423, -49.2837, "Bar do Blues, Batel", 1, 21, free = false),
        ev(14, "Orquestra no Parque", EventCategory.SHOW, "Concerto ao ar livre com repertório popular e clássicos brasileiros.",
            -25.4224, -49.3103, "Parque Barigui, Curitiba", 3, 17, free = true, hot = true),
        ev(15, "Rock na Pedreira", EventCategory.SHOW, "Bandas independentes ocupam o palco da Pedreira Paulo Leminski.",
            -25.3899, -49.2700, "Pedreira Paulo Leminski, Curitiba", 9, 19, 30, free = false, hot = true),
        ev(16, "Samba de Raiz", EventCategory.SHOW, "Roda de samba tradicional com feijoada no almoço.",
            -25.4275, -49.2723, "Largo da Ordem, Curitiba", 5, 13, free = true),
        ev(17, "Sarau Acústico", EventCategory.SHOW, "Voz e violão com autorais de artistas da cena curitibana.",
            -25.4319, -49.2764, "Sesc da Esquina, Curitiba", 2, 19, free = true),
        ev(18, "Tarde de Choro", EventCategory.SHOW, "Chorinho ao vivo com músicos convidados no coreto.",
            -25.4268, -49.2705, "Passeio Público, Curitiba", 6, 16, free = true),
        ev(19, "Festival de Jazz no Jardim", EventCategory.SHOW, "Três palcos de jazz e soul entre as estufas do Jardim Botânico.",
            -25.4426, -49.2385, "Jardim Botânico, Curitiba", 12, 15, free = false, hot = true),
        ev(20, "Rap na Praça", EventCategory.SHOW, "Batalha de MCs e shows abertos na Praça do Japão.",
            -25.4390, -49.2802, "Praça do Japão, Curitiba", 4, 18, free = true),

        // ---- Festas ----
        ev(21, "Baile Retrô Anos 80", EventCategory.FESTA, "Só hits dos anos 80, pista de dança e DJ residente.",
            -25.4410, -49.2820, "Clube do Baile, Batel", 4, 22, free = false),
        ev(22, "Open Air Sunset", EventCategory.FESTA, "Festa ao pôr do sol com DJs e food trucks.",
            -25.3862, -49.2710, "Parque São Lourenço, Curitiba", 7, 16, free = false, hot = true),
        ev(23, "Forró da Vila", EventCategory.FESTA, "Aula de forró às 20h e baile com trio pé-de-serra.",
            -25.4548, -49.2757, "Casa do Forró, Água Verde", 2, 20, free = false),
        ev(24, "Noite Latina", EventCategory.FESTA, "Salsa, bachata e reggaeton com aulão de dança para iniciantes.",
            -25.4310, -49.2740, "Bar Latino, Centro", 8, 21, free = false),
        ev(25, "Pagode do Bairro", EventCategory.FESTA, "Roda de pagode com o melhor do gênero e cerveja gelada.",
            -25.4753, -49.2965, "Praça do Portão, Curitiba", 3, 19, free = true),
        ev(26, "Techno Warehouse", EventCategory.FESTA, "Line-up de techno e house em galpão no Rebouças. 18+.",
            -25.4380, -49.2660, "Galpão Rebouças, Curitiba", 11, 23, 30, free = false),

        // ---- Teatro e dança ----
        ev(27, "Comédia: Casal em Crise", EventCategory.TEATRO, "Peça de humor sobre as confusões de um casal moderno.",
            -25.4271, -49.2653, "Teatro Guaíra, Curitiba", 2, 20, free = false),
        ev(28, "Balé: O Quebra-Nozes", EventCategory.TEATRO, "Clássico de fim de ano pela companhia de balé da cidade.",
            -25.4271, -49.2653, "Teatro Guaíra, Curitiba", 14, 19, 30, free = false, hot = true),
        ev(29, "Mostra de Teatro de Rua", EventCategory.TEATRO, "Grupos de teatro de rua ocupam a Rua XV com esquetes gratuitas.",
            -25.4297, -49.2719, "Rua XV de Novembro, Curitiba", 5, 15, free = true),
        ev(30, "Musical Infantil", EventCategory.TEATRO, "Musical para toda a família, com músicas ao vivo.",
            -25.4365, -49.2688, "Teatro do Paiol, Curitiba", 6, 11, free = false),
        ev(31, "Noite de Improviso", EventCategory.TEATRO, "Comédia de improviso com a plateia escolhendo os temas.",
            -25.4300, -49.2750, "Bar Central, Curitiba", 9, 21, free = false),
        ev(32, "Dança Contemporânea", EventCategory.TEATRO, "Coreografias autorais de jovens companhias locais.",
            -25.4319, -49.2764, "Sesc da Esquina, Curitiba", 10, 19, free = true),

        // ---- Arte e cultura ----
        ev(33, "Exposição de Grafite", EventCategory.ARTE, "Painéis de artistas urbanos ocupam as paredes do centro histórico.",
            -25.4283, -49.2748, "Rua Trajano Reis, São Francisco", 1, 10, free = true, hot = true),
        ev(34, "Oficina de Aquarela", EventCategory.ARTE, "Curso de introdução à aquarela para iniciantes, material incluso.",
            -25.4340, -49.2700, "Ateliê Central, Curitiba", 4, 15, free = false),
        ev(35, "Noite dos Museus", EventCategory.ARTE, "Museus abertos à noite com visitas guiadas e música.",
            -25.4106, -49.2670, "Museu Oscar Niemeyer, Curitiba", 8, 18, free = true),
        ev(36, "Feira de Ilustração", EventCategory.ARTE, "Ilustradores independentes vendem zines, prints e quadrinhos.",
            -25.4400, -49.2800, "Espaço Cultural Batel, Curitiba", 7, 11, free = true),
        ev(37, "Caminhada Fotográfica", EventCategory.ARTE, "Passeio guiado pelo centro com dicas de composição e luz.",
            -25.4308, -49.2735, "Praça Osório, Curitiba", 3, 9, free = true),
        ev(38, "Cerâmica Contemporânea", EventCategory.ARTE, "Obras de ceramistas paranaenses distribuídas em três salas.",
            -25.4106, -49.2670, "Museu Oscar Niemeyer, Curitiba", 13, 10, free = false),
        ev(39, "Sarau de Poesia", EventCategory.ARTE, "Microfone aberto para poemas, contos e crônicas.",
            -25.4318, -49.2727, "Café Literário, Centro", 5, 19, 30, free = true),

        // ---- Esportes ----
        ev(40, "Pedalada Noturna", EventCategory.ESPORTES, "Passeio ciclístico de 15 km pelas ciclovias da cidade.",
            -25.4283, -49.2707, "Praça Tiradentes, Curitiba", 2, 19, 30, free = true, hot = true),
        ev(41, "Corrida do Parque 10K", EventCategory.ESPORTES, "Percurso de 10 km em volta do Parque Barigui.",
            -25.4224, -49.3103, "Parque Barigui, Curitiba", 6, 7, free = false),
        ev(42, "Yoga ao Nascer do Sol", EventCategory.ESPORTES, "Aula aberta de yoga entre as estufas do Jardim Botânico.",
            -25.4426, -49.2385, "Jardim Botânico, Curitiba", 3, 7, 30, free = true),
        ev(43, "Vôlei de Praia Amador", EventCategory.ESPORTES, "Torneio de duplas com premiação. Inscrições no local.",
            -25.3862, -49.2710, "Parque São Lourenço, Curitiba", 10, 9, free = true),
        ev(44, "Clássico Amador de Futsal", EventCategory.ESPORTES, "Final do campeonato amador da cidade, com torcida organizada.",
            -25.4438, -49.2545, "Ginásio do Tarumã, Curitiba", 7, 20, free = false),
        ev(45, "Campeonato de Skate", EventCategory.ESPORTES, "Skate street com pista montada na Praça do Japão.",
            -25.4390, -49.2802, "Praça do Japão, Curitiba", 12, 14, free = true),

        // ---- Gastronomia ----
        ev(46, "Festival de Pizza", EventCategory.GASTRONOMIA, "Pizzarias da cidade em um só lugar, com degustação.",
            -25.3776, -49.2884, "Parque Tanguá, Curitiba", 6, 12, free = false, hot = true),
        ev(47, "Noite do Vinho", EventCategory.GASTRONOMIA, "Degustação de vinhos brasileiros harmonizados com queijos.",
            -25.4423, -49.2837, "Adega do Batel, Curitiba", 4, 20, free = false),
        ev(48, "Cupping de Café Especial", EventCategory.GASTRONOMIA, "Prova guiada de cafés de pequenos produtores.",
            -25.4315, -49.2716, "Torrefação Central, Curitiba", 2, 16, free = false),
        ev(49, "Feijoada Beneficente", EventCategory.GASTRONOMIA, "Almoço com renda revertida para projetos sociais.",
            -25.3945, -49.3231, "Santa Felicidade, Curitiba", 8, 12, 30, free = false),
        ev(50, "Cerveja Artesanal Fest", EventCategory.GASTRONOMIA, "Cervejarias artesanais, food trucks e música ao vivo.",
            -25.3868, -49.3040, "Parque Tingui, Curitiba", 9, 14, free = false, hot = true),
        ev(51, "Tour Gastronômico do Centro", EventCategory.GASTRONOMIA, "Roteiro a pé por 5 paradas de comida de rua do centro.",
            -25.4489, -49.2603, "Mercado Municipal, Curitiba", 5, 10, 30, free = false),

        // ---- Feiras ----
        ev(52, "Feira Orgânica do Batel", EventCategory.FEIRA, "Produtores locais com hortifrúti orgânico e pães artesanais.",
            -25.4423, -49.2837, "Av. do Batel, Curitiba", 1, 7, free = true),
        ev(53, "Feira de Antiguidades", EventCategory.FEIRA, "Discos, livros e objetos vintage no Largo da Ordem.",
            -25.4275, -49.2723, "Largo da Ordem, Curitiba", 7, 9, free = true),
        ev(54, "Feira de Troca de Livros", EventCategory.FEIRA, "Traga um livro, leve outro. Mesa de troca e rodas de leitura.",
            -25.4335, -49.2726, "Praça Rui Barbosa, Curitiba", 4, 10, free = true),
        ev(55, "Feira Criativa Handmade", EventCategory.FEIRA, "Artesãos de toda a região expõem e vendem suas peças.",
            -25.4405, -49.2699, "Estação Cultural, Curitiba", 13, 10, free = true, hot = true),
        ev(56, "Feira Pet Friendly", EventCategory.FEIRA, "Adoção responsável, produtos pet e estandes de veterinários.",
            -25.3862, -49.2710, "Parque São Lourenço, Curitiba", 15, 10, free = true),

        // ---- Gratuitos ----
        ev(57, "Cinema na Praça", EventCategory.GRATUITO, "Sessão gratuita de cinema nacional com pipoca liberada.",
            -25.4308, -49.2735, "Praça Osório, Curitiba", 3, 19, 30, free = true),
        ev(58, "Aulão de Dança de Rua", EventCategory.GRATUITO, "Aula aberta de street dance para todos os níveis.",
            -25.4390, -49.2802, "Praça do Japão, Curitiba", 2, 18, 30, free = true),
        ev(59, "Contação de Histórias", EventCategory.GRATUITO, "Histórias para crianças com fantoches e música.",
            -25.4021, -49.2586, "Bosque do Papa, Curitiba", 5, 10, 30, free = true),
        ev(60, "Mutirão de Plantio", EventCategory.GRATUITO, "Ação voluntária de plantio de árvores nativas, com mudas fornecidas.",
            -25.3868, -49.3040, "Parque Tingui, Curitiba", 9, 8, 30, free = true, hot = true),
        ev(61, "Visita Guiada ao Centro Histórico", EventCategory.GRATUITO, "Caminhada pelo Largo da Ordem, Rua XV e Passeio Público.",
            -25.4275, -49.2723, "Largo da Ordem, Curitiba", 4, 10, free = true),
        ev(62, "Oficina de Xadrez para Iniciantes", EventCategory.GRATUITO, "Aula prática para quem nunca jogou, com tabuleiros emprestados.",
            -25.4262, -49.2648, "Biblioteca Pública, Curitiba", 6, 15, free = true)
    )

    val events: List<Event> = curated + more

    private fun ev(
        n: Int, name: String, category: EventCategory, description: String,
        lat: Double, lng: Double, address: String,
        daysFromNow: Int, hour: Int, minute: Int = 0,
        free: Boolean, hot: Boolean = false
    ): Event {
        val id = "evt_%03d".format(n)
        return Event(
            id, name, category, description,
            lat = lat, lng = lng, address = address,
            date = LocalDate.now().plusDays(daysFromNow.toLong()), time = LocalTime.of(hour, minute),
            distanceMeters = metersFromCenter(lat, lng), isFree = free, isHighlighted = hot,
            imageUrl = coverUrl(id)
        )
    }

    // Distância (haversine) até o centro de Curitiba, arredondada pra 10 m.
    private fun metersFromCenter(lat: Double, lng: Double): Int {
        val r = 6_371_000.0
        val dLat = Math.toRadians(lat - Constants.CURITIBA_LAT)
        val dLng = Math.toRadians(lng - Constants.CURITIBA_LNG)
        val sinLat = Math.sin(dLat / 2)
        val sinLng = Math.sin(dLng / 2)
        val a = sinLat * sinLat +
            Math.cos(Math.toRadians(Constants.CURITIBA_LAT)) * Math.cos(Math.toRadians(lat)) * sinLng * sinLng
        val meters = 2 * r * Math.asin(Math.sqrt(a))
        return (Math.round(meters / 10.0) * 10).toInt()
    }

    // BETA: foto de capa determinística por evento (Picsum, sem API key, sem billing).
    // Mesma seed sempre retorna a mesma imagem — não é rede "aleatória" a cada load.
    private fun coverUrl(eventId: String): String = "https://picsum.photos/seed/$eventId/600/750"

    // Usuário de teste pré-cadastrado — email: teste@patamar.app / senha: Teste@123
    fun testUser(): User {
        val salt = "patamar_beta_salt_2024"
        return User(
            id = "usr_test_001",
            name = "Usuário Teste",
            email = "teste@patamar.app",
            passwordHash = PasswordHasher.hash("Teste@123", salt),
            salt = salt
        )
    }
}
