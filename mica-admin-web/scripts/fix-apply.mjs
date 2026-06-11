import fs from 'fs';
import path from 'path';

const classToCss = {
  'text-foreground': 'color: hsl(var(--foreground));',
  'text-foreground/80': 'color: hsl(var(--foreground) / 80%);',
  'text-primary': 'color: hsl(var(--primary));',
  'text-primary-foreground': 'color: hsl(var(--primary-foreground));',
  'text-muted-foreground': 'color: hsl(var(--muted-foreground));',
  'font-semibold': 'font-weight: 600;',
  'bg-primary': 'background-color: hsl(var(--primary));',
  'bg-accent': 'background-color: hsl(var(--accent));',
  'bg-accent-hover': 'background-color: hsl(var(--accent-hover));',
  'bg-heavy': 'background-color: hsl(var(--heavy));',
  'max-h-5': 'max-height: 1.25rem;',
  'mt-2': 'margin-top: 0.5rem;',
  'h-7': 'height: 1.75rem;',
  'pl-3.75': 'padding-left: 0.9375rem;',
  'pr-3.75': 'padding-right: 0.9375rem;',
  'pl-1.25': 'padding-left: 0.3125rem;',
  'pr-2': 'padding-right: 0.5rem;',
  'py-0': 'padding-top: 0; padding-bottom: 0;',
  'flex': 'display: flex;',
  'items-center': 'align-items: center;',
  'relative': 'position: relative;',
  'absolute': 'position: absolute;',
  '-ml-3': 'margin-left: -0.75rem;',
  '-ml-1': 'margin-left: -0.25rem;',
  'mr-9': 'margin-right: 2.25rem;',
  'rounded-l-sm': 'border-top-left-radius: 0.125rem; border-bottom-left-radius: 0.125rem;',
  'rounded-r-sm': 'border-top-right-radius: 0.125rem; border-bottom-right-radius: 0.125rem;',
  'border-none': 'border: none;',
  'border-accent': 'border-color: hsl(var(--accent));',
  'border-accent-hover': 'border-color: hsl(var(--accent-hover));',
  'border-l-transparent': 'border-left-color: transparent;',
  'border-l-accent': 'border-left-color: hsl(var(--accent));',
  'border-l-accent-hover': 'border-left-color: hsl(var(--accent-hover));',
  'border-14': 'border-width: 3.5px;',
  'border-solid': 'border-style: solid;',
  'content-[""]': 'content: "";',
  'z-10': 'z-index: 10;',
  'cursor-pointer': 'cursor: pointer;',
  'opacity-0': 'opacity: 0;',
  'opacity-0!': 'opacity: 0 !important;',
  'z-[2]': 'z-index: 2;',
  '-top-3': 'top: -0.75rem;',
  'z-9999': 'z-index: 9999;',
  'sticky': 'position: sticky;',
  'scale-50': 'transform: scale(0.5);',
};

function processFile(filePath) {
  try {
    let content = fs.readFileSync(filePath, 'utf8');
    
    // Remove @reference lines
    content = content.replace(/@reference\s+"@vben\/tailwind-config\/theme";?\n?/g, '');
    
    // Replace @apply class; or @apply class;
    content = content.replace(/@apply\s+([^;]+);/g, (match, classes) => {
      const classList = classes.trim().split(/\s+/);
      const cssDeclarations = [];
      
      for (const cls of classList) {
        if (classToCss[cls]) {
          cssDeclarations.push(classToCss[cls]);
        } else {
          console.log('Unknown class:', cls, 'in', path.basename(filePath));
        }
      }
      
      return cssDeclarations.join(' ');
    });
    
    fs.writeFileSync(filePath, content);
    console.log('Processed:', path.basename(filePath));
  } catch (e) {
    console.error('Error processing', filePath, e.message);
  }
}

// Process Vue files in _vben
function walkDir(dir) {
  if (!fs.existsSync(dir)) return;
  const files = fs.readdirSync(dir);
  for (const file of files) {
    const fullPath = path.join(dir, file);
    const stat = fs.statSync(fullPath);
    if (stat.isDirectory() && !fullPath.includes('node_modules')) {
      walkDir(fullPath);
    } else if (file.endsWith('.vue')) {
      processFile(fullPath);
    }
  }
}

walkDir('_vben');
console.log('Done!');
