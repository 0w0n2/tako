const fs = require('fs');
const path = require('path');

const inputFilePath = path.join(__dirname, '..', 'card_names.txt');
const outputFilePath = path.join(__dirname, '..', 'card_translations.json');

fs.readFile(inputFilePath, 'utf8', (err, data) => {
  if (err) {
    console.error('Error reading card_names.txt:', err);
    return;
  }

  const lines = data.trim().split('\n');
  const translations = {};

  lines.forEach(line => {
    line = line.trim();
    let firstKoreanIndex = -1;
    for (let i = 0; i < line.length; i++) {
      if (line[i] >= '\uac00' && line[i] <= '\ud7a3') {
        firstKoreanIndex = i;
        break;
      }
    }

    if (firstKoreanIndex !== -1) {
      const englishName = line.substring(0, firstKoreanIndex).trim();
      const koreanName = line.substring(firstKoreanIndex).trim();
      if (englishName) {
        translations[englishName] = koreanName;
      }
    }
  });

  fs.writeFile(outputFilePath, JSON.stringify(translations, null, 2), 'utf8', (err) => {
    if (err) {
      console.error('Error writing to card_translations.json:', err);
      return;
    }
    console.log('Successfully created card_translations.json');
  });
});