document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    let tab = urlParams.get('tab');

    if (!tab) {
        tab = 'info'; // 기본값 설정
        window.history.replaceState({}, '', `?tab=${tab}`); // URL 업데이트
    }

    loadTabContent(tab); // 탭에 맞는 내용 로드
});

function loadTabContent(tabName) {
    // 모든 sidebar-item에서 active 클래스 제거
    document.querySelectorAll('.mypage-sidebar-item').forEach(item => {
        item.classList.remove('active');
    });

    // 클릭한 요소에 active 클래스 추가
    document.getElementById(tabName).classList.add('active');


    // 탭 내용 가져오기
    fetch(`/api/mypage/${tabName}`)
        .then(response => response.json())
        .then(data => {
            document.getElementById('mainSection').innerHTML = generateContent(tabName, data);

            if (tabName === 'activities') {
                const urlParams = new URLSearchParams(window.location.search);
                let menu = urlParams.get('menu') || 'mypost'; // 기본값: 'mypost'
                loadMenuContent(menu); // menu 로드
            }

        })
        .catch(error => {
            console.error('Error fetching data:', error);
        });

    // URL 업데이트
    window.history.pushState({}, '', `?tab=${tabName}`);
}

function loadMenuContent(menuName) {
    // 모든 sidebar-item에서 active 클래스 제거
    document.querySelectorAll('.menu-item').forEach(item => {
        item.classList.remove('active');
    });

    // 클릭한 요소에 active 클래스 추가
    document.getElementById(menuName).classList.add('active');

    // 탭 내용 가져오기
    fetch(`/api/mypage/activities/${menuName}`)
        .then(response => response.json())
        .then(data => {
            document.getElementById('tableContainer').innerHTML = generateContent(menuName, data);
        })
        .catch(error => {
            console.error('Error fetching data:', error);
        });

    // URL 업데이트 (activities 탭 안에서 menu 변경)
    window.history.pushState({}, '', `?tab=activities&menu=${menuName}`);
}


function generateContent(contentName, data) {
    let content = '';
    if (contentName === 'info') {
        content = `
    <div class="title-container">
      <h1>내 정보</h1>
    </div>

    <div class="content-container">

      <div class="profile-container">
        <div class="profile-image">
            <img src=${data.profileImgUrl} alt="프로필 이미지">
            
            
            <input type="file" id="profile-input" style="display: none;" accept="image/*">
            <div class="profile-image-controls">
                <button type="button" class="profile-btn img-delete-btn" title="삭제" onclick="deleteProfileImage()">
                    <i class="fas fa-trash"></i>
                </button>
                <button type="button" class="profile-btn img-edit-btn" title="수정" onclick="openImageSelector()">
                    <i class="fas fa-camera"></i>
                </button>
            </div>
        </div>

        <div class="profile-info">
          <div class="info-row upper-info-row">
            <div class="info-label upper-info-label">이름</div>
            <div class="info-content upper-info-content" id="name">${data.name}</div>
          </div>
          <div class="info-row upper-info-row">
            <div class="info-label upper-info-label">사번</div>
            <div class="info-content upper-info-content" id="empNum">${data.empNum}</div>
          </div>
        </div>
      </div>

      <div class="info-container">
        <div class="info-row">
          <div class="info-label">입사일</div>
          <div class="info-content" id="hireDate">${data.hireDate}</div>
        </div>

        <div class="info-row">
          <div class="info-label">소속</div>
          <div class="info-content" id="departmentName">${data.departmentName}</div>
        </div>

        <div class="info-row">
          <div class="info-label">직급</div>
          <div class="info-content" id="positionTitle">${data.positionTitle}</div>
        </div>

        <div class="info-row">
          <div class="info-label">전화번호</div>
          <div class="info-content">
            <input type="text" id="phone" value="${data.phone}">
          </div>
        </div>

        <div class="info-row">
          <div class="info-label">이메일</div>
          <div class="info-content" id="internalEmail">${data.internalEmail}</div>
        </div>

        <div class="info-row">
          <div class="info-label">개인 이메일</div>
          <div class="info-content">
            <input type="email" id="email" value="${data.email}">
          </div>
        </div>

        <button class="submit-button" onclick="updateInfo()">저장</button>
      </div>
    </div>
`;

    } else if (contentName === 'activities') {
        content = `
    <div class="title-container">
      <h1>나의 활동</h1>
    </div>

    <div class="menu-container">
      <div class="menu-item active" id="mypost" onclick="loadMenuContent('mypost')">작성글</div>
      <div class="menu-item" id="mycomment" onclick="loadMenuContent('mycomment')">작성댓글</div>
      <div class="menu-item" id="starredpost" onclick="loadMenuContent('starredpost')">중요게시글</div>
    </div>
    
    <div class="table-container" id="tableContainer">

    </div class="table-container">      
`;

    } else if (contentName === 'security') {
        content = `
    <div class="title-container">
      <h1>보안</h1>
    </div>
    
    <div class="info-container">
        <div class="info-row">
          <div class="info-label">마지막 활동</div>
          <div class="info-content" id="lastLogin">${data.lastLogin}</div>
        </div>
        <div class="info-row">
          <div class="info-label">비밀번호 변경</div>
          <div class="info-content pw-change"><a href="/auth/change-password" target="_blank">변경하기</a></div>
        </div>
    </div>  
    
`;
    } else if (contentName === 'mypost') {
        content = `
    <table>
      <thead>
      <tr>
        <th class="checkbox-col post-th"></th>
        <th class="board-col post-th">게시판</th>
        <th class="title-col post-th">제목</th>
        <th class="created-at-col post-th">작성일</th>
        <th class="views-col post-th">조회수</th>
      </tr>
      </thead>
      
      <tbody>
      <tr>
        <td class="checkbox-col post-td"><input type="checkbox"></td>
        <td class="board-col post-td">자유게시판</td>
        <td class="title-col post-td">제목입니다!!!!!</td>
        <td class="created-at-col post-td">2025.03.28</td>
        <td class="views-col post-td">26</td>
      </tr>
      <tr>
        <td class="checkbox-col post-td"><input type="checkbox"></td>
        <td class="board-col post-td">자유게시판</td>
        <td class="title-col post-td">제목입니다!!!!!</td>
        <td class="created-at-col post-td">2025.03.28</td>
        <td class="views-col post-td">26</td>
      </tr>
      <tr>
        <td class="checkbox-col post-td"><input type="checkbox"></td>
        <td class="board-col post-td">자유게시판</td>
        <td class="title-col post-td">제목입니다!!!!!</td>
        <td class="created-at-col post-td">2025.03.28</td>
        <td class="views-col post-td">26</td>
      </tr>
      <tr>
        <td class="checkbox-col post-td"><input type="checkbox"></td>
        <td class="board-col post-td">자유게시판</td>
        <td class="title-col post-td">제목입니다!!!!!</td>
        <td class="created-at-col post-td">2025.03.28</td>
        <td class="views-col post-td">26</td>
      </tr>
      <tr>
        <td class="checkbox-col post-td"><input type="checkbox"></td>
        <td class="board-col post-td">자유게시판</td>
        <td class="title-col post-td">제목입니다!!!!!</td>
        <td class="created-at-col post-td">2025.03.28</td>
        <td class="views-col post-td">26</td>
      </tr>

      </tbody>
    </table>

    <div style="margin-top: 20px;">
      <input type="checkbox" id="selectAll">
      <label for="selectAll">전체선택</label>
      <button class="delete-btn">삭제</button>
    </div>        
        `;
    }else if (contentName === 'mycomment'){
        content = `<h2>작성댓글</h2>`;
    }else if (contentName === 'starredpost'){
        content = `<h2>중요게시글</h2>`;
    }
    return content;
}



// 카메라 아이콘 클릭 시 파일 선택 창 열기
function openImageSelector() {
    document.getElementById('profile-input').click();
}

// 프로필 이미지 삭제 플래그
let isImageDeleted = false;

// 파일 선택 시 이미지 미리보기 및 업로드 준비
document.addEventListener('change', function(event) {
    if (event.target.id === 'profile-input') {
        const file = event.target.files[0];
        if (file) {
            // 파일 유효성 검사 (이미지 파일인지)
            if (!file.type.startsWith('image/')) {
                alert('이미지 파일만 선택할 수 있습니다.');
                return;
            }

            // 파일 크기 제한 (예: 5MB)
            if (file.size > 5 * 1024 * 1024) {
                alert('파일 크기는 5MB 이하여야 합니다.');
                return;
            }

            isImageDeleted = false;

            // 이미지 미리보기
            const reader = new FileReader();
            reader.onload = function(e) {
                document.querySelector('.profile-image img').src = e.target.result;
            };
            reader.readAsDataURL(file);
        }
    }
});

// 프로필 이미지 삭제 함수
function deleteProfileImage() {
    document.querySelector('.profile-image img').src = '/assets/images/default-profile.png'; // 기본 이미지로 변경
    document.getElementById('profile-input').value = ''; // 파일 선택 초기화
    isImageDeleted = true;
    console.log(isImageDeleted);
}

// 내 정보 업데이트
function updateInfo() {
    const phoneInput = document.getElementById("phone");
    const emailInput = document.getElementById("email");
    const profileInput = document.getElementById('profile-input');

    // 전화번호 유효성 검사 (숫자와 하이픈만 허용)
    const phonePattern = /^010-\d{4}-\d{4}$/; // 010-1234-5678 형식
    const emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

    if (!phonePattern.test(phoneInput.value)) {
        alert('전화번호는 010-XXXX-XXXX 형식으로 입력해주세요.');
        phoneInput.focus();
        return;
    }

    if (!emailPattern.test(emailInput.value)) {
        alert('유효한 이메일 주소를 입력해주세요.');
        emailInput.focus();
        return;
    }

    // FormData 생성
    const formData = new FormData();
    formData.append('phone', phoneInput.value);
    formData.append('email', emailInput.value);

    if (profileInput.files.length > 0) {
        formData.append('profileImage', profileInput.files[0]);
    }

    formData.append("isImageDeleted", isImageDeleted);

    // update 처리
    fetch('/api/mypage/update', {
        method: 'PATCH',
        body: formData
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('서버 응답 오류');
            }
            return response.json();
        })
        .then(data => {
            alert('정보가 성공적으로 업데이트되었습니다.');
        })
        .catch(error => {
            alert('정보 업데이트 중 오류가 발생했습니다.');
            console.error(error); // 오류 로그 출력
        });
}
