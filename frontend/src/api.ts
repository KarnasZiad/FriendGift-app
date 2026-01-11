import type { FriendDto, GiftIdeaDto, LoginResponse } from './types';

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? 'http://127.0.0.1:8080';

export class ApiError extends Error {
  status: number;

  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

function getToken(): string | null {
  return localStorage.getItem('friendgift.token');
}

export function setToken(token: string | null) {
  if (token) {
    localStorage.setItem('friendgift.token', token);
  } else {
    localStorage.removeItem('friendgift.token');
  }
}

export function isAuthenticated(): boolean {
  return Boolean(getToken());
}

async function requestJson<T>(path: string, init?: RequestInit): Promise<T> {
  const headers = new Headers(init?.headers);
  headers.set('Accept', 'application/json');

  const token = getToken();
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers,
  });

  if (!response.ok) {
    if (response.status === 401 || response.status === 403) {
      // token expired/invalid
      setToken(null);
    }
    throw new ApiError(response.status, `HTTP ${response.status}`);
  }

  // 204 safety
  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

export async function login(username: string, password: string): Promise<LoginResponse> {
  return await requestJson<LoginResponse>('/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json; charset=utf-8',
    },
    body: JSON.stringify({ username, password }),
  });
}

export async function register(username: string, password: string): Promise<LoginResponse> {
  return await requestJson<LoginResponse>('/api/auth/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json; charset=utf-8',
    },
    body: JSON.stringify({ username, password }),
  });
}

export async function listFriends(): Promise<FriendDto[]> {
  return await requestJson<FriendDto[]>('/api/friends');
}

export async function createFriend(name: string): Promise<FriendDto> {
  return await requestJson<FriendDto>('/api/friends', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json; charset=utf-8',
    },
    body: JSON.stringify({ name }),
  });
}

export async function updateFriend(friendId: string, name: string): Promise<FriendDto> {
  return await requestJson<FriendDto>(`/api/friends/${encodeURIComponent(friendId)}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json; charset=utf-8',
    },
    body: JSON.stringify({ name }),
  });
}

export async function deleteFriend(friendId: string): Promise<void> {
  await requestJson<void>(`/api/friends/${encodeURIComponent(friendId)}`, {
    method: 'DELETE',
  });
}

export async function listIdeas(friendId: string): Promise<GiftIdeaDto[]> {
  return await requestJson<GiftIdeaDto[]>(`/api/friends/${encodeURIComponent(friendId)}/ideas`);
}

export async function addIdea(friendId: string, text: string): Promise<GiftIdeaDto> {
  return await requestJson<GiftIdeaDto>(`/api/friends/${encodeURIComponent(friendId)}/ideas`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json; charset=utf-8',
    },
    body: JSON.stringify({ text }),
  });
}
