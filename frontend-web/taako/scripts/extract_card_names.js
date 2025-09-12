const fs = require('fs');
const path = require('path');

const cardsFilePath = path.join(__dirname, '..', 'components', 'cards', 'cards.json');
const outputFilePath = path.join(__dirname, '..', 'card_names.txt');

fs.readFile(cardsFilePath, 'utf8', (err, data) => {
  if (err) {
    console.error('Error reading cards.json:', err);
    return;
  }

  try {
    const cards = JSON.parse(data);
    const names = cards.map(card => card.name).join('\n');

    fs.writeFile(outputFilePath, names, 'utf8', (err) => {
      if (err) {
        console.error('Error writing to card_names.txt:', err);
        return;
      }
      console.log('Successfully extracted card names to card_names.txt');
    });
  } catch (parseError) {
    console.error('Error parsing JSON from cards.json:', parseError);
  }
});
