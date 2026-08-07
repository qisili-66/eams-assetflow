<script setup lang="ts">
defineProps<{
  title: string
}>()
</script>

<template>
  <section class="page">
    <h1>{{ title }}</h1>
    <p>这是{{ title }}页面</p>
  </section>
</template>

<style scoped>
.page {
  padding: 24px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
}

h1 {
  margin: 0 0 8px;
  color: #1f2937;
  font-size: 24px;
}

p {
  margin: 0;
  color: #64748b;
}
</style>