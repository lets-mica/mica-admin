<script lang="ts" setup>
import { computed } from 'vue';

import { useNamespace } from '@vben-core/composables';

const props = defineProps<{
  size?: number | string;
}>();

const { bem } = useNamespace('empty-icon');

const size = computed(() => {
  if (typeof props.size === 'number') {
    return `${props.size}px`;
  }
  return props.size || '1em';
});
</script>

<template>
  <span :class="bem()" :style="{ fontSize: size }">
    <svg viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
      <title>Empty Icon</title>
      <g fill="none" fill-rule="evenodd">
        <rect
          x="6"
          y="6"
          width="52"
          height="52"
          rx="6"
          fill="#F2F3F5"
        />
        <g stroke="#3370FF" stroke-width="2.4" stroke-linecap="round">
          <path d="M22 28h20" />
          <path d="M22 36h12" />
        </g>
      </g>
    </svg>
  </span>
</template>