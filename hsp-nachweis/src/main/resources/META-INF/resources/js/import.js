/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

if ("customElements" in window) {

  class HspDataImport extends HTMLElement {
    constructor() {
      super();

      if (document.head.createShadowRoot || document.head.attachShadow) {

        const template = document.createElement("template");
        const parser = new DOMParser();
        const shadowRoot = this.attachShadow({mode: 'open'});

        window.fetch(
            this.getAttribute('baseURL') + '/' + this.getAttribute('basePath'))
        .then(res => res.text()
        ).then(html => {

          const doc = parser.parseFromString(html, "text/html");
          const importElement = doc.getElementById("hsp-data-import");

          if (importElement) {

            const links = doc.getElementsByTagName("link");
            const scripts = doc.getElementsByTagName("script");
            const forms = doc.getElementsByTagName("form");

            this.manipulateFormAction(forms);
            template.innerHTML = importElement.innerHTML;

            this.manipulateLinkHrefs(links, shadowRoot);

            this.manipulateScriptSrc(scripts, shadowRoot);
          } else {
            template.innerHTML = `<div>Importmodul konnte nicht gefunden werden</div>`;
          }

          shadowRoot.appendChild(
              document.importNode(template.content, true));

        }).catch(function (error) {
          console.log(error);

          template.innerHTML = `<div>Importmodul konnte nicht geladen werden <span>${error}</span></div>`;
          shadowRoot.appendChild(
              document.importNode(template.content, true));
        });
      }
    }

    manipulateScriptSrc(scripts, shadowRoot) {
      Array.prototype.forEach.call(scripts, s => {

        const script = document.createElement('script')

        script.async = false;
        script.defer = true;
        script.setAttribute('src',
            this.getAttribute('baseURL') + s.getAttribute('src')
        )

        shadowRoot.appendChild(script);

      });
    }

    manipulateLinkHrefs(links, shadowRoot) {
      Array.prototype.forEach.call(links, l => {

        if (!l.getAttribute('href').includes("http")) {

          const link = document.createElement('link')
          link.setAttribute('rel', 'stylesheet')
          link.setAttribute('href',
              this.getAttribute('baseURL') + l.getAttribute('href'))

          shadowRoot.appendChild(link);
        } else {
          const link = document.createElement('link')
          link.setAttribute('rel', 'stylesheet')
          link.setAttribute('href', l.getAttribute('href'))
          shadowRoot.appendChild(link);
        }

      });
    }

    manipulateFormAction(forms) {
      Array.prototype.forEach.call(forms, f => {

        f.action =
            this.getAttribute('baseURL') + '/' + this.getAttribute(
            'basePath')

      });
    }
  }

  if (!customElements.get('hsp-data-import')) {
    customElements.define('hsp-data-import', HspDataImport);
  }

}