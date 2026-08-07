<template>
  <main class="not-found">
    <h1>404</h1>
    <p>页面不存在</p>
    <RouterLink to="/dashboard">返回主页</RouterLink>
  </main>
</template>

<style scoped>
.not-found {
  display: grid;
  min-height: 100vh;
  place-content: center;
  justify-items: center;
  gap: 12px;
  background: #f5f7fa;
}

h1,
p {
  margin: 0;
  color: #1f2937;
}

a {
  color: #0f766e;
  text-decoration: none;
}
</style>