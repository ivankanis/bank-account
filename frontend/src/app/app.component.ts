import { Component } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  template: `
    <nav class="navbar">
      <a routerLink="/" class="brand">BankAccount</a>
    </nav>
    <main>
      <router-outlet />
    </main>
  `,
  styles: [`
    .navbar { background: #2d6a4f; padding: 1rem 2rem; }
    .brand { color: white; font-size: 1.25rem; font-weight: 700; text-decoration: none; }
    main { padding: 1rem; }
  `]
})
export class AppComponent {}
