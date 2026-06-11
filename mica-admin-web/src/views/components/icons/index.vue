<script setup lang="ts">
import { NCard, NTabs, NTabPane, NInput, NIcon, NGrid, NGi, NTag } from 'naive-ui';
import {
  ArrowDown,
  ArrowLeft,
  ArrowRight,
  ArrowUp,
  Bell,
  Bold,
  BookOpenText,
  Check,
  ChevronDown,
  ChevronLeft,
  ChevronRight,
  Circle,
  CircleAlert,
  CircleCheckBig,
  CircleHelp,
  CircleX,
  Copy,
  Download,
  Ellipsis,
  Eraser,
  Expand,
  ExternalLink,
  Eye,
  EyeOff,
  FoldHorizontal,
  Fullscreen,
  Grid,
  Grip,
  GripVertical,
  ImagePlus,
  Inbox,
  Info,
  Italic,
  Languages,
  LayoutGrid,
  Link2,
  List,
  ListOrdered,
  LoaderCircle,
  LockKeyhole,
  LogOut,
  MailCheck,
  Maximize,
  Menu,
  MessageSquareCode,
  Minimize,
  Minimize2,
  MoonStar,
  Paintbrush,
  Palette,
  PanelLeft,
  PanelRight,
  Pin,
  PinOff,
  Plus,
  RotateCw,
  Search as SearchIcon,
  Settings,
  Shrink,
  Square,
  Strikethrough,
  Sun,
  SunMoon,
  Underline,
  UserRoundPen,
  X,
} from '@lucide/vue';
import { computed, ref, type Component } from 'vue';
import { notification } from '#/adapter/naive';

defineOptions({ name: 'IconsIndex' });

const searchText = ref('');

interface IconItem {
  name: string;
  component: Component;
}

// Vben 图标列表（@vben/icons / @lucide/vue），直接以组件方式渲染
const vbenIconList: IconItem[] = [
  { name: 'Search', component: SearchIcon },
  { name: 'Bell', component: Bell },
  { name: 'Settings', component: Settings },
  { name: 'UserRoundPen', component: UserRoundPen },
  { name: 'Plus', component: Plus },
  { name: 'Check', component: Check },
  { name: 'X', component: X },
  { name: 'CircleX', component: CircleX },
  { name: 'CircleCheckBig', component: CircleCheckBig },
  { name: 'CircleAlert', component: CircleAlert },
  { name: 'CircleHelp', component: CircleHelp },
  { name: 'Info', component: Info },
  { name: 'Eye', component: Eye },
  { name: 'EyeOff', component: EyeOff },
  { name: 'LockKeyhole', component: LockKeyhole },
  { name: 'LogOut', component: LogOut },
  { name: 'MailCheck', component: MailCheck },
  { name: 'MessageSquareCode', component: MessageSquareCode },
  { name: 'Menu', component: Menu },
  { name: 'LayoutGrid', component: LayoutGrid },
  { name: 'Grid', component: Grid },
  { name: 'List', component: List },
  { name: 'ListOrdered', component: ListOrdered },
  { name: 'BookOpenText', component: BookOpenText },
  { name: 'Inbox', component: Inbox },
  { name: 'Copy', component: Copy },
  { name: 'Eraser', component: Eraser },
  { name: 'Download', component: Download },
  { name: 'ImagePlus', component: ImagePlus },
  { name: 'Palette', component: Palette },
  { name: 'Paintbrush', component: Paintbrush },
  { name: 'Pin', component: Pin },
  { name: 'PinOff', component: PinOff },
  { name: 'Languages', component: Languages },
  { name: 'MoonStar', component: MoonStar },
  { name: 'Sun', component: Sun },
  { name: 'SunMoon', component: SunMoon },
  { name: 'ExternalLink', component: ExternalLink },
  { name: 'Link2', component: Link2 },
  { name: 'LoaderCircle', component: LoaderCircle },
  { name: 'RotateCw', component: RotateCw },
  { name: 'ChevronDown', component: ChevronDown },
  { name: 'ChevronLeft', component: ChevronLeft },
  { name: 'ChevronRight', component: ChevronRight },
  { name: 'ArrowUp', component: ArrowUp },
  { name: 'ArrowDown', component: ArrowDown },
  { name: 'ArrowLeft', component: ArrowLeft },
  { name: 'ArrowRight', component: ArrowRight },
  { name: 'Maximize', component: Maximize },
  { name: 'Minimize', component: Minimize },
  { name: 'Minimize2', component: Minimize2 },
  { name: 'Fullscreen', component: Fullscreen },
  { name: 'Expand', component: Expand },
  { name: 'Shrink', component: Shrink },
  { name: 'PanelLeft', component: PanelLeft },
  { name: 'PanelRight', component: PanelRight },
  { name: 'FoldHorizontal', component: FoldHorizontal },
  { name: 'Grip', component: Grip },
  { name: 'GripVertical', component: GripVertical },
  { name: 'Ellipsis', component: Ellipsis },
  { name: 'Circle', component: Circle },
  { name: 'Square', component: Square },
  { name: 'Bold', component: Bold },
  { name: 'Italic', component: Italic },
  { name: 'Underline', component: Underline },
  { name: 'Strikethrough', component: Strikethrough },
];

// Naive-UI 图标列表（同一批 lucide 组件，但通过 NIcon 包装使用）
const naiveIconList: IconItem[] = vbenIconList;

const filteredVbenIcons = computed(() => {
  if (!searchText.value) return vbenIconList;
  const kw = searchText.value.toLowerCase();
  return vbenIconList.filter(i => i.name.toLowerCase().includes(kw));
});

const filteredNaiveIcons = computed(() => {
  if (!searchText.value) return naiveIconList;
  const kw = searchText.value.toLowerCase();
  return naiveIconList.filter(i => i.name.toLowerCase().includes(kw));
});

function generateVbenCode(name: string) {
  return `<${name} />`;
}

function generateNaiveCode(name: string) {
  return `<NIcon :size="20"><${name} /></NIcon>`;
}

function copyCode(code: string) {
  navigator.clipboard.writeText(code).then(() => {
    notification.success({ content: '已复制', description: code, duration: 1500 });
  }).catch(() => {
    notification.warning({ content: '复制失败，请手动复制', duration: 2000 });
  });
}
</script>

<template>
  <div class="p-4">
    <NCard title="图标选择器">
      <template #header-extra>
        <NInput v-model:value="searchText" placeholder="搜索图标" size="small" style="width: 200px">
          <template #prefix><NIcon><SearchIcon /></NIcon></template>
        </NInput>
      </template>

      <NTabs type="line" animated>
        <NTabPane name="vben" tab="Vben 图标">
          <div class="text-sm text-gray-500 mb-3 flex items-center gap-2">
            <span>共 {{ filteredVbenIcons.length }} 个图标，点击复制代码。</span>
            <NTag size="small">@vben/icons</NTag>
          </div>
          <NGrid :x-gap="8" :y-gap="8" cols="2 400:4 600:6 800:8 1000:10">
            <NGi v-for="icon in filteredVbenIcons" :key="icon.name">
              <div
                class="icon-item p-3 text-center cursor-pointer hover:bg-blue-50 rounded transition-colors"
                @click="copyCode(generateVbenCode(icon.name))"
              >
                <div class="w-10 h-10 mx-auto flex items-center justify-center mb-2 text-gray-700">
                  <component :is="icon.component" :size="22" />
                </div>
                <div class="text-xs text-gray-700 truncate">{{ icon.name }}</div>
              </div>
            </NGi>
          </NGrid>
        </NTabPane>

        <NTabPane name="naive" tab="Naive-UI 图标">
          <div class="text-sm text-gray-500 mb-3 flex items-center gap-2">
            <span>共 {{ filteredNaiveIcons.length }} 个图标，点击复制代码。</span>
            <NTag size="small" type="success">NIcon</NTag>
          </div>
          <NGrid :x-gap="8" :y-gap="8" cols="2 400:4 600:6 800:8 1000:10">
            <NGi v-for="icon in filteredNaiveIcons" :key="icon.name">
              <div
                class="icon-item p-3 text-center cursor-pointer hover:bg-blue-50 rounded transition-colors"
                @click="copyCode(generateNaiveCode(icon.name))"
              >
                <div class="w-10 h-10 mx-auto flex items-center justify-center mb-2 text-blue-500">
                  <NIcon :size="22">
                    <component :is="icon.component" />
                  </NIcon>
                </div>
                <div class="text-xs text-gray-700 truncate">{{ icon.name }}</div>
              </div>
            </NGi>
          </NGrid>
        </NTabPane>
      </NTabs>
    </NCard>
  </div>
</template>

<style scoped>
:deep(.n-card) {
  border-radius: 8px;
}

.icon-item {
  border: 1px solid transparent;
}

.icon-item:hover {
  border-color: #3b82f6;
}
</style>
