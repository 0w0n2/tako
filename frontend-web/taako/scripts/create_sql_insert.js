const fs = require('fs');
const path = require('path');

const cardsFilePath = path.join(__dirname, '..', 'components', 'cards', 'cards.json');
const translationsFilePath = path.join(__dirname, '..', 'card_translations.json');
const outputFilePath = path.join(__dirname, '..', 'cards.sql');

const cards = JSON.parse(fs.readFileSync(cardsFilePath, 'utf8'));
const translations = JSON.parse(fs.readFileSync(translationsFilePath, 'utf8'));

const insertStatements = cards.map(card => {
  const code = card.id;
  const description = JSON.stringify({ subtypes: card.subtypes });
  const attribute = 'NULL';
  const rarity = card.rarity;
  const category_major_id = 4;

  let category_medium_id;
  if (card.supertype === 'Pokémon') {
    category_medium_id = 12;
  } else if (card.supertype === 'Trainer') {
    if (card.subtypes.includes('Supporter')) {
      category_medium_id = 11;
    } else if (card.subtypes.includes('Item')) {
      category_medium_id = 9;
    } else if (card.subtypes.includes('Stadium')) {
      category_medium_id = 10;
    }
  }

  const images = card.images && card.images.large ? card.images.large.split('/').pop() : '';
  const name = translations[card.name] || card.name;

  // Escape single quotes in string values
  const escape = (str) => str ? str.replace(/'/g, "''") : '';

  return `INSERT INTO tcg.card (code, description, attribute, rarity, category_major_id, category_medium_id, name) VALUES ('${escape(code)}', '${escape(description)}', ${attribute}, '${escape(rarity)}', ${category_major_id}, ${category_medium_id || 'NULL'}, '${escape(name)}');`;
});

fs.writeFileSync(outputFilePath, insertStatements.join('\n'), 'utf8');
console.log('Successfully created cards.sql');
