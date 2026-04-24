import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import DOMPurify from 'dompurify';

@Pipe({
  name: 'safeHtml',
  standalone: true
})
export class SafeHtmlPipe implements PipeTransform {

  constructor(private sanitizer: DomSanitizer) {}

  transform(value: string): SafeHtml {
    if (!value) {
      return '';
    }
    // Strict sanitization via DOMPurify to prevent XSS
    const sanitizedHtml = DOMPurify.sanitize(value, {
      ALLOWED_TAGS: ['b', 'i', 'em', 'strong', 'a', 'p', 'br', 'ul', 'ol', 'li', 'span', 'div'],
      ALLOWED_ATTR: ['href', 'target', 'class', 'style']
    });
    
    // Tell Angular the string is safe to bypass its own sanitization
    return this.sanitizer.bypassSecurityTrustHtml(sanitizedHtml);
  }
}
