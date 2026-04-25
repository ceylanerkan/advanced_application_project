import { Component, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SafeHtmlPipe } from '../../../core/pipes/safe-html.pipe';
import { PlotlyModule, PlotlyService } from 'angular-plotly.js';
import * as PlotlyJS from 'plotly.js-dist-min';
import { AuthService } from '../../../core/services/auth.service';

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
  messages: ChatMessage[] = [
    { id: 1, text: 'Hello! I am your AI Text2SQL assistant. Ask me anything about your e-commerce data.', isUser: false }
  ];

  constructor(private router: Router, private authService: AuthService) {}

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  scrollToBottom(): void {
    try {
      this.scrollContainer.nativeElement.scrollTop = this.scrollContainer.nativeElement.scrollHeight;
    } catch(err) { }
  }

  sendMessage() {
    if (!this.userInput.trim()) return;

    // Add User Message
    this.messages.push({
      id: Date.now(),
      text: this.userInput,
      isUser: true
    });

    const query = this.userInput.toLowerCase();
    this.userInput = '';

    // Mock AI Response with Plotly Data
    setTimeout(() => {
      let aiResponse: ChatMessage = {
        id: Date.now(),
        text: 'Here is the data you requested:',
        isUser: false
      };

      if (query.includes('revenue') || query.includes('sales')) {
        aiResponse.plotData = {
          data: [{ x: ['Jan', 'Feb', 'Mar'], y: [20, 14, 23], type: 'bar', marker: {color: '#3b82f6'} }],
          layout: { 
            title: 'Q1 Revenue', 
            paper_bgcolor: 'rgba(0,0,0,0)', 
            plot_bgcolor: 'rgba(0,0,0,0)',
            font: { color: '#f8fafc' }
          }
        };
      } else {
        aiResponse.text = "I executed the following SQL query:<br><br><code>SELECT * FROM users;</code><br><br>Found 10,000 users.";
      }

      this.messages.push(aiResponse);
    }, 1000);
  }

  goBack() {
    const role = this.authService.currentUserValue?.role;
    if (role === 'ADMIN') this.router.navigate(['/admin']);
    else if (role === 'CORPORATE') this.router.navigate(['/corporate']);
    else this.router.navigate(['/individual']);
  }
}
