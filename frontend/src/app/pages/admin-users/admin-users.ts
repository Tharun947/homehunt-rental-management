import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Role, UserSummary } from '../../core/models/api.models';
import { AdminService } from '../../core/services/admin.service';

@Component({
  imports: [FormsModule],
  templateUrl: './admin-users.html',
  styleUrl: './admin-users.scss'
})
export class AdminUsersPage {
  users = signal<UserSummary[]>([]);
  pendingRoles = signal<Record<number, Role>>({});
  message = signal('');

  constructor(private admin: AdminService) {
    this.load();
  }

  load() {
    this.admin.users().subscribe((users) => this.users.set(users));
  }

  setPendingRole(userId: number, role: Role) {
    this.pendingRoles.update((roles) => ({ ...roles, [userId]: role }));
  }

  selectedRole(user: UserSummary) {
    return Object.prototype.hasOwnProperty.call(this.pendingRoles(), user.id) ? this.pendingRoles()[user.id] : user.role;
  }

  applyRole(user: UserSummary) {
    const role = this.selectedRole(user);
    this.admin.updateRole(user.id, role).subscribe(() => {
      this.message.set(`${user.email} role updated to ${role}.`);
      this.pendingRoles.update((roles) => {
        const next = { ...roles };
        delete next[user.id];
        return next;
      });
      this.load();
    });
  }

  deleteUser(id: number) {
    this.admin.deleteUser(id).subscribe(() => {
      this.message.set('User deleted.');
      this.load();
    });
  }
}
