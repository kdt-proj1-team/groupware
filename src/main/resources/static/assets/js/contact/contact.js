document.addEventListener('DOMContentLoaded', function () {
    const urlParams = new URLSearchParams(window.location.search);

    if (!urlParams.has('tab')) {
        urlParams.set('tab', 'shared');
        urlParams.set('dept', 'all');
        history.replaceState(null, '', window.location.pathname + '?' + urlParams.toString());
    }

    const tab = urlParams.get('tab');
    const dept = urlParams.get('dept') || 'all';

    fetchDepartments();  // 부서 목록 로드
    loadContacts(tab, dept);  // 초기 연락처 로드
    updateActiveSidebarItem(tab, dept);  // active 클래스 초기화

    // 주소 추가 버튼
    document.querySelector('.add-contact-btn').addEventListener('click', function () {
        const modal = document.getElementById('contact-modal');
        modal.removeAttribute('data-mode');
        modal.removeAttribute('data-id');

        modal.classList.remove('hidden');
    });

    // 주소 추가 취소 버튼
    document.querySelector('.contact-cancel-btn').addEventListener('click', function () {
        resetContactForm()

        document.getElementById('contact-modal').classList.add('hidden');
    });

    // Esc 키 눌렀을 때 모달 닫기
    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') {
            const contactModal = document.getElementById('contact-modal');
            const detailModal = document.getElementById('contact-detail-modal');

            if (!contactModal.classList.contains('hidden')) {
                resetContactForm();
                contactModal.classList.add('hidden');
            }

            if (!detailModal.classList.contains('hidden')) {
                detailModal.classList.add('hidden');
            }
        }
    });


    //  개인 주소록 연락처 등록 메모 글자 수 업데이트
    document.getElementById('memoInput').addEventListener('input', function () {
        const currentLength = this.value.length;
        document.getElementById('char-count').textContent = currentLength;
    });


    // 개인주소록 주소 저장
    document.getElementById('saveContactBtn').addEventListener('click', function () {
        console.log("버튼 클릭");
        const name = document.getElementById('nameInput').value.trim();
        const email = document.getElementById('emailInput').value;
        const phone = document.getElementById('phoneInput').value;
        const memo = document.getElementById('memoInput').value;

        if(!name) {
            alert('이름은 필수 입력 항목입니다.');
            document.getElementById('nameInput').focus();
            return;
        }

        const contactData = { name, email, phone, memo };

        const modal = document.getElementById('contact-modal');
        const mode = modal.getAttribute('data-mode');

        if (mode === 'edit') {
            const id = modal.getAttribute('data-id');

            fetch(`/api/contact/personal/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(contactData)
            })
                .then(res => {
                    if (res.ok) {
                        resetContactForm();
                        modal.classList.add('hidden');
                        loadContacts('personal', 'all');
                    } else {
                        alert('수정에 실패했습니다.');
                    }
                })
                .catch(error => {
                    console.error('수정 중 오류 발생:', error);
                    alert('서버 오류로 수정에 실패했습니다.');
                });

        } else {
            // 추가일 경우 기존 POST 요청 유지
            fetch('/api/contact/personal/add', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(contactData)
            })
                .then(res => {
                    resetContactForm();

                    if (res.status === 201) {
                        modal.classList.add('hidden');
                        loadContacts('personal', 'all');
                    } else {
                        alert('저장에 실패했습니다.');
                    }
                })
                .catch(error => {
                    console.error('저장 중 오류 발생:', error);
                    alert('서버 오류로 저장에 실패했습니다.');
                });
        }
    });

    // 개인 주소록 삭제 이벤트
    document.addEventListener('click', function (e) {
        if (e.target.id === 'deleteContactsBtn') {
            const checked = document.querySelectorAll('.contact-checkbox:checked');

            if (checked.length === 0) {
                alert('삭제할 항목을 선택해주세요.');
                return;
            }

            if (!confirm(`${checked.length}개의 주소를 삭제하시겠습니까?`)) {
                return;
            }

            // 체크된 항목들의 부모 tr에서 data-id 수집
            const ids = Array.from(checked).map(cb => Number(cb.closest('tr').dataset.id));

            fetch('/api/contact/personal/delete', {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(ids)
            })
                .then(response => {
                    if (response.ok) {
                        // 주소록 다시 불러오기
                        loadContacts('personal', 'all');
                        updateHeaderForSelection(); // 테이블 헤더 초기화
                    } else {
                        alert('삭제에 실패했습니다.');
                    }
                })
                .catch(error => {
                    console.error('삭제 중 오류 발생:', error);
                    alert('서버 오류로 삭제에 실패했습니다.');
                });
        }
    });


    // 개인 주소록 상세 보기 모달 수정 버튼 이벤트리스너
    document.getElementById('editContactBtn').addEventListener('click', function () {
        const contact = this.contactData;

        if (!contact) return;

        // input에 값 세팅
        document.getElementById('nameInput').value = contact.name || '';
        document.getElementById('emailInput').value = contact.email || '';
        document.getElementById('phoneInput').value = contact.phone || '';
        document.getElementById('memoInput').value = contact.memo || '';
        document.getElementById('char-count').textContent = contact.memo?.length || 0;

        // 수정 모드로 세팅
        const modal = document.getElementById('contact-modal');
        modal.setAttribute('data-mode', 'edit');
        modal.setAttribute('data-id', contact.id);

        document.getElementById('saveContactBtn').textContent = '수정';

        // 모달 전환
        document.getElementById('contact-detail-modal').classList.add('hidden');
        modal.classList.remove('hidden');
    });



    // 전체 선택 체크박스 이벤트
    document.addEventListener('change', function (e) {
        if (e.target.classList.contains('select-all-checkbox')) {
            const isChecked = e.target.checked;
            const checkboxes = document.querySelectorAll('.contact-checkbox');
            checkboxes.forEach(cb => cb.checked = isChecked);
        }
    });


    // 체크박스 변화 감지해서 헤더 동적으로 변경
    document.addEventListener('change', function (e) {
        // 개별 체크박스 or 전체 선택 체크박스일 때만 처리
        if (e.target.classList.contains('contact-checkbox') || e.target.classList.contains('select-all-checkbox')) {
            updateHeaderForSelection();
            updateSelectAllCheckbox();
        }
    });


});

// 개인 주소록 연락처 추가 input 초기화
function resetContactForm() {
    document.getElementById('nameInput').value = '';
    document.getElementById('emailInput').value = '';
    document.getElementById('phoneInput').value = '';
    document.getElementById('memoInput').value = '';
    document.getElementById('char-count').textContent = '0';
    document.getElementById('saveContactBtn').textContent = '저장';

}

// 부서 목록 불러오기
function fetchDepartments() {
    fetch('/api/departments')
        .then(response => {
            if (!response.ok) {
                throw new Error('부서 목록을 불러오는 데 실패했습니다.');
            }
            return response.json();
        })
        .then(departments => {
            const container = document.getElementById('shared-tab-container');
            departments.forEach(dept => {
                const div = document.createElement('div');
                div.className = 'contact-sidebar-item';
                div.textContent = dept.name;
                div.setAttribute('data-tab', 'shared');
                div.setAttribute('data-dept', dept.name);
                div.setAttribute('onclick', `handleSidebarClick('shared', '${dept.name}')`);
                container.appendChild(div);
            });
        })
        .catch(error => {
            console.error('부서 목록 로드 실패:', error);
        });
}

// 사이드바 클릭 함수
function handleSidebarClick(tab, dept = 'all') {
    const urlParams = new URLSearchParams();
    urlParams.set('tab', tab);

    // shared일 때만 dept 붙이기
    if (tab === 'shared') {
        urlParams.set('dept', dept);
    }

    history.pushState({}, '', window.location.pathname + '?' + urlParams.toString());
    loadContacts(tab, dept);
    updateActiveSidebarItem(tab, dept);
}

// 공유 or 개인 주소록 연락처 불러오기
function loadContacts(tab, dept) {
    let url = `/api/contact/${tab}`;
    if (tab === 'shared') {
        url += `?dept=${encodeURIComponent(dept)}`;
    }

    fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error("데이터를 불러오는 데 실패했습니다.");
            }
            return response.json();
        })
        .then(data => {
            renderContacts(data, tab);
            updateHeaderForSelection(); // 테이블 헤더 초기화
        })
        .catch(error => {
            console.error(error);
            document.querySelector("#contactList").innerHTML = `
              <tr><td colspan="5">주소록을 불러오는 데 실패했습니다.</td></tr>`;
        });
}

// 연락처 렌더링
function renderContacts(data, tab) {
    const tbody = document.getElementById('contactList');
    tbody.innerHTML = '';

    // info-col 헤더 변경
    document.querySelector('.contact-table thead .info-col').textContent = (tab === 'shared') ? '부서' : '메모';

    // 헤더 체크박스 동적 처리
    const headerCel = document.querySelector('.contact-table thead .cel');
    if (tab === 'personal') {
        headerCel.innerHTML = `<input type="checkbox" class="select-all-checkbox">`;
    } else {
        headerCel.innerHTML = ''; // 공유 탭이면 제거
    }


    if (!data.length) {
        tbody.innerHTML = `<tr><td colspan="5">표시할 데이터가 없습니다.</td></tr>`;
        return;
    }

    data.forEach(contact => {
        const tr = document.createElement('tr');

        tr.setAttribute('data-team', contact.depName || '');

        // 개인 연락처일 경우 data-id 속성 추가
        if (tab === 'personal') {
            tr.setAttribute('data-id', contact.id); // ✅ 추가
        }

        tr.innerHTML = `
            <td class="cel">
                ${tab === 'personal' ? `<input type="checkbox" class="contact-checkbox">` : ''}
            </td>
            <td class="name-col">${contact.name}</td>
            <td>${tab === 'shared' ? contact.internalEmail : contact.email || ''}</td>
            <td>${contact.phone}</td>
            <td class="info-col">${tab === 'shared' ? contact.depName : contact.memo || ''}</td>
        `;

        // 연락처 행 클릭 감시 (상세 보기 모달 띄우기 위해)
        tr.addEventListener('click', function (e) {
            // 체크박스 누를 땐 무시
            if (e.target.classList.contains('contact-checkbox')) return;
            openDetailModal(contact, tab);
        });

        tbody.appendChild(tr);
    });
}

// active 클래스 토글 전용 함수
function updateActiveSidebarItem(tab, dept) {
    document.querySelectorAll('.contact-sidebar-item').forEach(item => {
        const itemTab = item.getAttribute('data-tab');
        const itemDept = item.getAttribute('data-dept');
        if (itemTab === tab && itemDept === dept) {
            item.classList.add('active');
        } else {
            item.classList.remove('active');
        }
    });
}

// 브라우저 뒤로가기/앞으로가기 대응
window.addEventListener('popstate', function(event) {
    const urlParams = new URLSearchParams(window.location.search);
    const tab = urlParams.get('tab') || 'shared';
    let dept = urlParams.get('dept') || 'all';

    // personal이면 dept 무시
    if (tab === 'personal') {
        dept = 'all';
    }

    loadContacts(tab, dept);
    updateActiveSidebarItem(tab, dept);
});


// 체크 여부 판단
function updateSelectAllCheckbox() {
    const allCheckboxes = document.querySelectorAll('.contact-checkbox');
    const checkedCheckboxes = document.querySelectorAll('.contact-checkbox:checked');
    const selectAll = document.querySelector('.select-all-checkbox');

    if (!selectAll) return;

    // 체크된 게 0개면 전체 선택 체크박스도 해제
    if (checkedCheckboxes.length === 0) {
        selectAll.checked = false;
    }
}


// 개인주소록 연락처 체크박스 선택 시 버튼 처리
function updateHeaderForSelection() {
    const checkedCount = document.querySelectorAll('.contact-checkbox:checked').length;

    const thNameCol = document.querySelector('.contact-table thead .name-col');
    const thEmail = document.querySelector('.contact-table thead th:nth-child(3)');
    const thPhone = document.querySelector('.contact-table thead th:nth-child(4)');
    const thInfo = document.querySelector('.contact-table thead .info-col');

    // 현재 탭 확인
    const urlParams = new URLSearchParams(window.location.search);
    const tab = urlParams.get('tab') || 'shared';

    if (checkedCount > 0) {
        // 나머지 헤더는 텍스트만 숨김
        thEmail.textContent = '';
        thPhone.textContent = '';
        thInfo.textContent = '';

        // 이름 컬럼은 삭제 버튼으로 대체
        thNameCol.innerHTML = `<button id="deleteContactsBtn" class="delete-btn">삭제</button>`;
    } else {
        // 복원
        thEmail.textContent = '이메일';
        thPhone.textContent = '전화번호';
        thInfo.textContent = (tab === 'shared') ? '부서' : '메모';
        thNameCol.textContent = '이름';
    }
}

// 연락처 상세 보기 모달 열기 닫기
function openDetailModal(contact, tab) {
    setFieldVisibility('detailName', contact.name);
    setFieldVisibility('detailEmail', tab === 'shared' ? contact.internalEmail : contact.email);
    setFieldVisibility('detailPhone', contact.phone);

    const editBtn = document.getElementById('editContactBtn');

    if (tab === 'shared') {
        document.getElementById('sharedOnly').style.display = '';
        document.getElementById('personalOnly').style.display = 'none';

        setFieldVisibility('detailDept', contact.depName);
        setFieldVisibility('detailPosition', contact.posTitle);

        // 공유 주소록은 수정 버튼 숨김
        editBtn.classList.add('hidden');
    } else {
        document.getElementById('sharedOnly').style.display = 'none';
        document.getElementById('personalOnly').style.display = '';

        setFieldVisibility('detailMemo', contact.memo);

        // 개인 주소록은 수정 버튼 표시
        editBtn.classList.remove('hidden');
        editBtn.setAttribute('data-id', contact.id); // 수정 시 사용할 ID 저장
        editBtn.contactData = contact;
    }

    document.getElementById('contact-detail-modal').classList.remove('hidden');
}

// 연락처 상세 보기 모달 숨기기
function closeDetailModal() {
    document.getElementById('contact-detail-modal').classList.add('hidden');
}

// 값이 없으면 해당 form-group 숨기고, 값이 있으면 보여줌
function setFieldVisibility(fieldId, value) {
    const field = document.getElementById(fieldId);
    const group = field?.closest('.form-group');

    if (!field || !group) return;

    if (value && value.trim() !== '') {
        field.textContent = value;
        group.style.display = '';
    } else {
        group.style.display = 'none';
    }
}




