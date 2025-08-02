package com.psipro.app.utils

import com.psipro.app.data.entities.Autoavaliacao
import java.util.*

class AIMotivationalService {
    
    fun generateMotivationalMessage(autoavaliacao: Autoavaliacao): String {
        val score = autoavaliacao.scoreGeral
        val categoria = autoavaliacao.categoriaGeral
        val conquistas = autoavaliacao.conquistasMes
        val desafios = autoavaliacao.principaisDesafios
        val gratidao = autoavaliacao.gratidao
        
        return when (categoria) {
            "Excelente" -> generateExcellentMessage(score, conquistas, gratidao)
            "Bom" -> generateGoodMessage(score, conquistas, desafios)
            "Regular" -> generateRegularMessage(score, desafios, autoavaliacao.objetivosProximoMes)
            "Precisa Atenção" -> generateAttentionMessage(score, desafios)
            else -> generateDefaultMessage()
        }
    }
    
    private fun generateExcellentMessage(score: Float, conquistas: String, gratidao: String): String {
        val messages = listOf(
            "🌟 Parabéns! Você está em excelente forma emocional e profissional. Seu score de ${String.format("%.1f", score)} reflete um equilíbrio excepcional!",
            "🎉 Incrível! Você está no topo da sua forma. Continue cultivando essa energia positiva e compartilhando sua luz com os outros.",
            "✨ Você é um exemplo de bem-estar! Sua dedicação ao autocuidado está rendendo frutos maravilhosos. Continue assim!"
        )
        return messages.random()
    }
    
    private fun generateGoodMessage(score: Float, conquistas: String, desafios: String): String {
        val messages = listOf(
            "👍 Você está bem! Há espaço para melhorias, mas está no caminho certo. Continue investindo em si mesmo!",
            "💪 Bom trabalho! Seu score de ${String.format("%.1f", score)} mostra que você está equilibrado. Foque nas áreas que podem melhorar ainda mais.",
            "🌱 Você está crescendo! Cada pequeno progresso conta. Continue nutrindo suas conquistas e trabalhando nos desafios."
        )
        return messages.random()
    }
    
    private fun generateRegularMessage(score: Float, desafios: String, objetivos: String): String {
        val messages = listOf(
            "🔄 É normal ter altos e baixos. Que tal focar em uma área específica para melhorar? Pequenos passos levam a grandes mudanças.",
            "📈 Você tem potencial! Seu score de ${String.format("%.1f", score)} pode melhorar. Que tal definir um objetivo específico para este mês?",
            "💡 Momentos de transição são oportunidades de crescimento. Use seus desafios como combustível para suas conquistas."
        )
        return messages.random()
    }
    
    private fun generateAttentionMessage(score: Float, desafios: String): String {
        val messages = listOf(
            "🤗 Lembre-se: cuidar de si mesmo é fundamental para cuidar dos outros. Considere buscar apoio profissional ou conversar com um colega.",
            "💙 É corajoso reconhecer quando precisamos de ajuda. Seu bem-estar é prioridade. Que tal focar em uma área específica este mês?",
            "🌱 Todo jardim precisa de cuidados. Você merece atenção e carinho. Pequenas mudanças podem fazer uma grande diferença."
        )
        return messages.random()
    }
    
    fun generateDefaultMessage(): String {
        val messages = listOf(
            "🧠 Reflita sobre seu dia: Como você está se sentindo emocionalmente hoje?",
            "💭 Lembre-se: cuidar de si mesmo é fundamental para cuidar dos outros. Considere buscar apoio profissional ou conversar com um colega.",
            "🌟 Pause por um momento e respire fundo. Você está fazendo um trabalho importante. Como pode se cuidar melhor hoje?",
            "💪 Você dedica tanto tempo cuidando dos outros. Hoje, que tal dedicar alguns minutos para cuidar de si mesmo?",
            "🌱 O autocuidado não é egoísmo, é necessidade. Como você pode se nutrir emocionalmente hoje?",
            "✨ Você merece o mesmo cuidado e atenção que oferece aos seus pacientes. Que tal fazer uma pausa para se conectar consigo mesmo?",
            "💙 É normal se sentir sobrecarregado. Lembre-se de que você também precisa de suporte. Como está sua energia hoje?",
            "🌟 Você é um profissional incrível. Não se esqueça de celebrar suas conquistas, mesmo as pequenas.",
            "🧘‍♀️ Respire fundo e pergunte-se: O que eu preciso neste momento? Como posso me dar isso?",
            "💫 Você faz a diferença na vida de muitas pessoas. Não se esqueça de fazer a diferença na sua própria vida também."
        )
        return messages.random()
    }
    
    fun generateWeeklyTip(): String {
        val tips = listOf(
            "💡 Dica da semana: Reserve 10 minutos por dia para meditação ou respiração consciente.",
            "🌿 Dica da semana: Experimente uma nova atividade física esta semana - dança, yoga ou caminhada.",
            "📚 Dica da semana: Leia um livro que não seja relacionado ao trabalho por 15 minutos antes de dormir.",
            "🎨 Dica da semana: Dedique tempo a uma atividade criativa - desenho, música, culinária ou artesanato.",
            "🤝 Dica da semana: Conecte-se com um amigo ou familiar que você não vê há algum tempo.",
            "🌱 Dica da semana: Experimente uma nova receita ou tipo de comida que nunca provou antes.",
            "🎯 Dica da semana: Defina um pequeno objetivo pessoal para esta semana e celebre quando alcançá-lo.",
            "🌅 Dica da semana: Acorde 15 minutos mais cedo para ter um momento só seu antes do dia começar."
        )
        return tips.random()
    }
    
    fun generateMonthlyReflectionPrompt(): String {
        val prompts = listOf(
            "🤔 Reflexão do mês: Qual foi o momento mais desafiador e como você lidou com ele?",
            "💭 Reflexão do mês: O que você aprendeu sobre si mesmo este mês?",
            "🌟 Reflexão do mês: Qual foi sua maior conquista pessoal ou profissional?",
            "🌱 Reflexão do mês: Em que área você gostaria de crescer no próximo mês?",
            "💡 Reflexão do mês: Que hábito você gostaria de desenvolver ou abandonar?",
            "🎯 Reflexão do mês: Qual foi a situação que mais te fez crescer este mês?",
            "🤗 Reflexão do mês: Como você pode ser mais gentil consigo mesmo?",
            "✨ Reflexão do mês: O que você faria diferente se pudesse recomeçar o mês?"
        )
        return prompts.random()
    }
} 



