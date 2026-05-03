import { Component, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SafeHtmlPipe } from '../../../core/pipes/safe-html.pipe';
import { PlotlyModule, PlotlyService } from 'angular-plotly.js';
import * as PlotlyJS from 'plotly.js-dist-min';
import { AuthService } from '../../../core/services/auth.service';
import { ApiService } from '../../../core/services/api.service';

PlotlyService.setPlotly(PlotlyJS);

interface ChatMessage {
  id: number;
  text: string;
  isUser: boolean;
  plotData?: any;
}

@Component({
  selector: 'app-chatbot-ui',
  standalone: true,
  imports: [CommonModule, FormsModule, SafeHtmlPipe, PlotlyModule],
  templateUrl: './chatbot-ui.component.html',
  styleUrls: ['./chatbot-ui.component.scss']
})
export class ChatbotUiComponent implements AfterViewChecked {
  @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

  userInput = '';
  isLoading = false;
  private sessionId = `session-${Date.now()}`;

  messages: ChatMessage[] = [
    { id: 1, text: 'Hello! I am your AI Text2SQL assistant. Ask me anything about your e-commerce data.', isUser: false }
  ];

  constructor(private router: Router, private authService: AuthService, private apiService: ApiService) {}

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  scrollToBottom(): void {
    try {
      this.scrollContainer.nativeElement.scrollTop = this.scrollContainer.nativeElement.scrollHeight;
    } catch(err) { }
  }

  sendMessage() {
    if (!this.userInput.trim() || this.isLoading) return;

    this.messages.push({ id: Date.now(), text: this.userInput, isUser: true });
    const question = this.userInput;
    this.userInput = '';
    this.isLoading = true;

    this.apiService.askAI(question, this.sessionId).subscribe({
      next: (response: any) => {
        this.isLoading = false;
        const msg: ChatMessage = { id: Date.now(), text: '', isUser: false };

        if (response.error) {
          msg.text = `Error: ${response.error}`;
        } else {
          msg.text = response.finalAnswer || 'No response from AI.';
          if (response.visualizationCode) {
            try {
              msg.plotData = JSON.parse(response.visualizationCode);
            } catch {
              msg.text += `<br><br><code>${response.visualizationCode}</code>`;
            }
          }
        }
        this.messages.push(msg);
      },
      error: () => {
        this.isLoading = false;
        this.messages.push({
          id: Date.now(),
          text: 'The AI service is currently unavailable. Please try again later.',
          isUser: false
        });
      }
    });
  }

  goBack() {
    const role = this.authService.currentUserValue?.role;
    if (role === 'ADMIN') this.router.navigate(['/admin']);
    else if (role === 'CORPORATE') this.router.navigate(['/corporate']);
    else this.router.navigate(['/individual']);
  }
}
