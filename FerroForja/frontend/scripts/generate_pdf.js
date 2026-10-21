const puppeteer = require('puppeteer');
const path = require('path');

(async () => {
  const htmlPath = path.resolve(__dirname, '..', 'RESUMEN_EJECUTIVO_PROYECTO.html');
  const pdfPath = path.resolve(__dirname, '..', 'RESUMEN_EJECUTIVO_PROYECTO.pdf');
  const browser = await puppeteer.launch({args: ['--no-sandbox','--disable-setuid-sandbox']});
  const page = await browser.newPage();
  await page.goto(`file://${htmlPath}`, {waitUntil: 'networkidle0'});
  await page.pdf({path: pdfPath, format: 'A4', printBackground: true});
  await browser.close();
  console.log('PDF generated at', pdfPath);
})();
