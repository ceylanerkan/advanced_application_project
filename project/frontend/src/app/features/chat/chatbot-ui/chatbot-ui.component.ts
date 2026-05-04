import { Component, ElementRef, ViewChild, AfterViewChecked, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SafeHtmlPipe } from '../../../core/pipes/safe-html.pipe';
import { AuthService } from '../../../core/services/auth.service';
import { ApiService } from '../../../core/services/api.service';

interface ChatMessage {
  id: number;
  text: string;
  isUser: boolean;
  plotData?: any;
  plotId?: string;
}

const STORAGE_KEY = 'chatMessages';
const WELCOME_MESSAGE: ChatMessage = {
  id: 1,
  text: 'Hello! I am your AI assistant. Ask me anything about your e-commerce data (orders, revenue, products, customers…).',
  isUser: false
};

@Component({
  selector: 'app-chatbot-ui',
  standalone: true,
  imports: [CommonModule, FormsModule, SafeHtmlPipe],
  templateUrl: './chatbot-ui.component.html',
  styleUrls: ['./chatbot-ui.component.scss']
})
export class ChatbotUiComponent implements AfterViewChecked, OnInit {
  @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

  userInput = '';
  isLoading = false;
  private sessionId = `session-${Date.now()}`;
  private pendingPlots: Set<string> = new Set();

  messages: ChatMessage[] = [];

  constructor(private router: Router, private authService: AuthService, private apiService: ApiService) {}

  ngOnInit() {
    this.loadMessages();
  }

  // ── Persistence ──────────────────────────────────────────────────────────

  private loadMessages() {
    try {
      const stored = sessionStorage.getItem(STORAGE_KEY);
      if (stored) {
        this.messages = JSON.parse(stored);
        // Queue any stored chart messages for re-rendering
        this.messages.forEach(m => {
          if (m.plotId && m.plotData) {
            this.pendingPlots.add(m.plotId);
          }
        });
      } else {
        this.messages = [{ ...WELCOME_MESSAGE }];
      }
    } catch {
      this.messages = [{ ...WELCOME_MESSAGE }];
    }
  }

  private saveMessages() {
    try {
      // Strip plotData before storing to keep sessionStorage lean; charts
      // cannot be re-rendered from stored JSON anyway without the Plotly div
      // being present in the DOM, so we just keep the text.
      const toStore = this.messages.map(m => ({
        id: m.id,
        text: m.text,
        isUser: m.isUser
        // plotData and plotId intentionally omitted
      }));
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(toStore));
    } catch { /* storage quota — silently ignore */ }
  }

  // ── Lifecycle ─────────────────────────────────────────────────────────────

  ngAfterViewChecked() {
    this.scrollToBottom();
    // Render any pending Plotly charts after Angular's DOM update
    this.pendingPlots.forEach(plotId => {
      const el = document.getElementById(plotId);
      if (el && el.children.length === 0) {
        const msg = this.messages.find(m => m.plotId === plotId);
        if (msg?.plotData) {
          try {
            (window as any).Plotly?.newPlot(
              el,
              msg.plotData.data,
              {
                ...(msg.plotData.layout || {}),
                paper_bgcolor: 'transparent',
                plot_bgcolor: 'transparent',
                font: { color: '#f8fafc' }
              },
              { responsive: true, displayModeBar: false }
            );
            this.pendingPlots.delete(plotId);
          } catch(e) { /* retry next cycle */ }
        }
      }
    });
  }

  scrollToBottom(): void {
    try {
      this.scrollContainer.nativeElement.scrollTop = this.scrollContainer.nativeElement.scrollHeight;
    } catch(err) { }
  }

  // ── Messaging ─────────────────────────────────────────────────────────────

  sendMessage() {
    if (!this.userInput.trim() || this.isLoading) return;

    this.messages.push({ id: Date.now(), text: this.userInput, isUser: true });
    const question = this.userInput;
    this.userInput = '';
    this.isLoading = true;
    this.saveMessages();

    this.apiService.askAI(question, this.sessionId).subscribe({
      next: (response: any) => {
        this.isLoading = false;
        const msg: ChatMessage = { id: Date.now(), text: '', isUser: false };

        if (response.error) {
          msg.text = `⚠️ ${response.error}`;
        } else {
          msg.text = response.finalAnswer || 'No response from AI.';
          if (response.visualizationCode) {
            try {
              msg.plotData = JSON.parse(response.visualizationCode);
              msg.plotId = `plot-${msg.id}`;
              this.pendingPlots.add(msg.plotId);
            } catch {
              msg.text += `<br><br><code>${response.visualizationCode}</code>`;
            }
          }
        }
        this.messages.push(msg);
        this.saveMessages();
      },
      error: () => {
        this.isLoading = false;
        this.messages.push({
          id: Date.now(),
          text: '⚠️ The AI service is currently unavailable. Make sure the Python AI server is running.',
          isUser: false
        });
        this.saveMessages();
      }
    });
  }

  clearHistory() {
    this.messages = [{ ...WELCOME_MESSAGE }];
    this.saveMessages();
  }

  goBack() {
    const role = this.authService.currentUserValue?.role;
    if (role === 'ADMIN') this.router.navigate(['/admin']);
    else if (role === 'CORPORATE') this.router.navigate(['/corporate']);
    else this.router.navigate(['/individual']);
  }
}
