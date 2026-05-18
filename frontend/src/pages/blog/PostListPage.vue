<template>
  <div class="post-list-page">
    <h1 class="page-title">文章列表</h1>
  </div>
</template>

<style scoped>
.page-title {
  display: inline-block;
  font-family: var(--font-heading);
  font-size: 2em;
  font-weight: 700;
  color: #1a1a1a;
  background: var(--accent-yellow);
  padding: 6px 20px 2px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

[data-theme='dark'] .page-title {
  color: #1a1a1a;
}
</style>
