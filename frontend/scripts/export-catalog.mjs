import fs from 'node:fs/promises';
import os from 'node:os';
import path from 'node:path';
import {pathToFileURL} from 'node:url';
import ts from 'typescript';

const directory = await fs.mkdtemp(path.join(os.tmpdir(), 'desk-catalog-'));
try {
  const source = await fs.readFile('src/lib/syllabus.ts', 'utf8');
  const file = path.join(directory, 'syllabus.mjs');
  await fs.writeFile(file, ts.transpileModule(source, {compilerOptions: {module: ts.ModuleKind.ES2022, target: ts.ScriptTarget.ES2022}}).outputText);
  const {allTopics, optionals, literature} = await import(pathToFileURL(file));
  const catalog = {topicIds: allTopics.map(t => t.id), optionals: ['Undecided', ...optionals, ...literature.map(name => name + ' Literature')]};
  await fs.writeFile('../backend/src/main/resources/catalog.json', JSON.stringify(catalog, null, 2) + '\n');
  console.log(`Exported ${catalog.topicIds.length} topics for backend validation.`);
} finally { await fs.rm(directory, {recursive: true, force: true}); }
