<template>
    <div class="min-h-screen bg-gray-900 text-white">
        <header class="grid grid-cols-3 p-6 bg-slate-800 items-center shadow-2xl">
            <div>
                <h1 class="text-3xl font-bold">CTI</h1>
                <h2 class="text-3xl font-bold">Insigths</h2>
            </div>
            <nav class="justify-self-center flex gap-4">
                <a href="#" class="rounded-xl px-2 border-2 border-transparent hover:border-slate-700 transition transition-500">Sobre</a>
                <a href="#" class="rounded-xl px-2 border-2 border-transparent hover:border-slate-700 transition transition-500">Recursos</a>
                <a href="#" class="rounded-xl px-2 border-2 border-transparent hover:border-slate-700 transition transition-500">Contato</a>
            </nav>
            <RouterLink to="/login" class="bg-sky-700 px-3 py-1 justify-self-end rounded-full border-2 border-transparent
            hover:bg-white 
            hover:border-sky-700
            hover:text-sky-700 transition transition-500">Login</RouterLink>
        </header>
    </div>
</template>