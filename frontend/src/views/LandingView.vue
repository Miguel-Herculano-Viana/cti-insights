<template>
    <div class="min-h-screen bg-gray-900 text-white">
        <header class="grid grid-cols-3 p-6 bg-linear-to-r from-slate-800 via-slate-700 to-slate-800 items-center shadow-2xl">
            <p class="text-3xl font-bold">
                <span class="block">CTI</span>
                <span class="block">Insights</span>
            </p>
            <nav class="justify-self-center flex gap-4">
                <a href="#sobre" class="rounded-xl px-2 border-2 border-transparent hover:border-slate-700 transition duration-300">Sobre</a>
                <a href="#contato" class="rounded-xl px-2 border-2 border-transparent hover:border-slate-700 transition duration-300">Contato</a>
            </nav>
            <RouterLink to="/Login" class="bg-sky-700 px-3 py-1 justify-self-end rounded-full border-2 border-transparent
            hover:bg-white 
            hover:border-sky-700
            hover:text-sky-700 transition duration-300">Login</RouterLink>
        </header>
        <main>
            <section id="hero" class="grid grid-cols-1 min-h-[70vh] p-30 gap-0 bg-linear-to-b from-blue-900 via-gray-900 to-sky-900 items-center md:grid-cols-2">
                <h1 class="font-bold text-5xl text-white justify-self-center text-center md:text-right">Transforme<br>Dados<br>em<br>Decisões</h1>
                <div class="bg-white/10 backdrop-blur-md border border-white/20 shadow-xl p-10 rounded-xl">
                    <img src="/cti-logo.png" alt="CTI" class="h-30 block mx-auto">
                    <p class="text-xl text-white text-center">Com CTI Insights, tudo fica mais fácil, todos os dados que importam para o seu crescimento a alguns cliques de distância</p>
                    <RouterLink to="/Login" class="bg-sky-700 px-3 py-1 block w-fit mx-auto rounded-full border-2 border-transparent mt-6
                    hover:bg-white 
                    hover:border-sky-700
                    hover:text-sky-700 transition duration-300">Acessar Sistema</RouterLink>
                </div>
            </section>
            <section id="sobre" class="bg-linear-to-b from-sky-900 via-gray-900 to-blue-900 border-t-2 border-black py-16 flex flex-col gap-6 text-center">
                <h2 class="font-bold text-2xl">Sobre o CTI Insights</h2>
                <p class="text-lg">CTI Insights é um sistema web que converte planilhas de dados extensos em tabelas e gráficos, transformando a informação em algo visual e fácil de entender</p>
                <div class="grid grid-cols-1 max-w-400 w-full mx-auto py-24 px-2 sm:grid-cols-2 gap-4">
                    <article class="bg-red-400/10 backdrop-blur-md border border-white/20 shadow-xl p-10 rounded-xl flex flex-col justify-center">
                        <h3 class="font-bold text-2xl text-center text-red-200">O Problema</h3>
                        <p class="text-gray-200 text-center text-lg">Hoje, a base de clientes é mantida em planilhas preenchidas manualmente. Esse processo abre espaço para erros de digitação e inconsistências, como o mesmo segmento escrito de formas diferentes (“IND.” e “Industria”). Com isso, analisar e cruzar os dados se torna lento e pouco confiável.
                        </p>
                    </article>
                    <article class="bg-cyan-400/10 backdrop-blur-md border border-white/20 shadow-xl p-10 rounded-xl flex flex-col justify-center">
                        <h3 class="font-bold text-2xl text-center text-cyan-200">A Solução</h3>
                        <p class="text-gray-200 text-center text-lg">Com o CTI Insights, basta enviar a planilha. O sistema valida e padroniza os dados automaticamente e aponta o que precisa ser corrigido. Em seguida, transforma a base tratada em gráficos e dashboards interativos, prontos para análise.</p>
                    </article>
                </div>
            </section>
        </main>
        <footer id="contato" class="bg-black">
            <address class="not-italic text-center">
                <span>contato: </span>
                <a href="mailto:miguel.viana9@senaisp.edu.br">miguel.viana9@senaisp.edu.br</a>
            </address>
            <p class="text-white text-xs text-center">SENAI® 2026</p>
        </footer>
    </div>
</template>