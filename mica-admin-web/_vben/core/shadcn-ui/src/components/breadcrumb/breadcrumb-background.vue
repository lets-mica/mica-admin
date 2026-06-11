<script lang="ts" setup>
import type { BreadcrumbProps } from './types';

import { VbenIcon } from '../icon';

interface Props extends BreadcrumbProps {}

defineOptions({ name: 'Breadcrumb' });
const { breadcrumbs, showIcon } = defineProps<Props>();

const emit = defineEmits<{ select: [string] }>();

function handleClick(index: number, path?: string) {
  if (!path || index === breadcrumbs.length - 1) {
    return;
  }
  emit('select', path);
}
</script>
<template>
  <ul class="flex">
    <TransitionGroup name="breadcrumb-transition">
      <template
        v-for="(item, index) in breadcrumbs"
        :key="`${item.path}-${item.title}-${index}`"
      >
        <li>
          <a
            href="javascript:void 0"
            @click.stop="handleClick(index, item.path)"
          >
            <span class="flex-center z-10 h-full">
              <VbenIcon
                v-if="showIcon"
                :icon="item.icon"
                class="mr-1 size-4 shrink-0"
              />
              <span
                :class="{
                  'text-foreground font-normal':
                    index === breadcrumbs.length - 1,
                }"
                >{{ item.title }}
              </span>
            </span>
          </a>
        </li>
      </template>
    </TransitionGroup>
  </ul>
</template>
<style scoped>

li {
  height: 1.75rem;
}

li a {
  background-color: hsl(var(--accent)); color: hsl(var(--muted-foreground)); position: relative; margin-right: 2.25rem; display: flex; height: 1.75rem; align-items: center; padding-top: 0; padding-bottom: 0; padding-right: 0.5rem; padding-left: 0.3125rem;
}

li a > span {
  margin-left: -0.75rem;
}

li:first-child a > span {
  margin-left: -0.25rem;
}

li:first-child a {
  border-top-left-radius: 0.125rem; border-bottom-left-radius: 0.125rem; padding-left: 0.9375rem;
}

li:first-child a::before {
  border: none;
}

li:last-child a {
  border-top-right-radius: 0.125rem; border-bottom-right-radius: 0.125rem; padding-right: 0.9375rem;
}

li:last-child a::after {
  border: none;
}

li a::before,
li a::after {
  border-color: hsl(var(--accent)); position: absolute; border-width: 3.5px; border-style: solid;
}

li a::before {
  z-index: 10; border-left-color: transparent;
}

li a::after {
  border-left-color: hsl(var(--accent));
}

li:not(:last-child) a:hover {
  background-color: hsl(var(--accent-hover));
}

li:not(:last-child) a:hover::before {
  border-color: hsl(var(--accent-hover)); border-left-color: transparent;
}

li:not(:last-child) a:hover::after {
  border-left-color: hsl(var(--accent-hover));
}
</style>
