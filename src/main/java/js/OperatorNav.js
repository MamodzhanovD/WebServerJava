import React, {Component} from 'react'
import ConnectionState from "./ConnectionState";
import {getRequest} from "../lib/connections/http/requests";
import {dateFormatterWithDotDelimeter, timeFormatter} from "../lib/formatters";
import logo from '../static/images/logo_small_header.png'
import {DarkModeButton} from "../components/DarkModeButton";

class OperatorNav extends Component {
    constructor(props) {
        super(props);
        this.state = {
            currentDate: new Date(),
            userName: undefined,
            countNotifications: 0,
            notifications: [],
            notificationsHidden: true,
            darkMode: false
        };
    }

    componentDidMount() {
        getRequest('/api/user_name/', user => {
            this.setState({userName: user.userName})
        })
        this.clock = setInterval(this.updateClock, 100);
    }

    updateClock = () => {
        this.setState({currentDate: new Date()});
    }


    darkMode = () => {
        let bodyBackgroundColor = getComputedStyle(document.documentElement).getPropertyValue('--body-background-color').trim();
        let bodyBackgroundColorDark = getComputedStyle(document.documentElement).getPropertyValue('--body-background-color-dark').trim();
        document.documentElement.style.setProperty('--body-background-color-dark', bodyBackgroundColor);
        document.documentElement.style.setProperty('--body-background-color', bodyBackgroundColorDark);

        let bodyTextColor = getComputedStyle(document.documentElement).getPropertyValue('--body-text-color').trim();
        let bodyTextColorDark = getComputedStyle(document.documentElement).getPropertyValue('--body-text-color-dark').trim();
        document.documentElement.style.setProperty('--body-text-color-dark', bodyTextColor);
        document.documentElement.style.setProperty('--body-text-color', bodyTextColorDark);

        let windowHeaderBackgroundColor = getComputedStyle(document.documentElement).getPropertyValue('--window-header-background-color').trim();
        let windowHeaderBackgroundColorDark = getComputedStyle(document.documentElement).getPropertyValue('--window-header-background-color-dark').trim();
        document.documentElement.style.setProperty('--window-header-background-color-dark', windowHeaderBackgroundColor);
        document.documentElement.style.setProperty('--window-header-background-color', windowHeaderBackgroundColorDark);

        let windowHeaderTextColor = getComputedStyle(document.documentElement).getPropertyValue('--window-header-text-color').trim();
        let windowHeaderTextColorDark = getComputedStyle(document.documentElement).getPropertyValue('--window-header-text-color-dark').trim();
        document.documentElement.style.setProperty('--window-header-text-color-dark', windowHeaderTextColor);
        document.documentElement.style.setProperty('--window-header-text-color', windowHeaderTextColorDark);

        let windowBodyBackgroundColor = getComputedStyle(document.documentElement).getPropertyValue('--window-body-background-color').trim();
        let windowBodyBackgroundColorDark = getComputedStyle(document.documentElement).getPropertyValue('--window-body-background-color-dark').trim();
        document.documentElement.style.setProperty('--window-body-background-color-dark', windowBodyBackgroundColor);
        document.documentElement.style.setProperty('--window-body-background-color', windowBodyBackgroundColorDark);

        let windowBodyTextColor = getComputedStyle(document.documentElement).getPropertyValue('--window-body-text-color').trim();
        let windowBodyTextColorDark = getComputedStyle(document.documentElement).getPropertyValue('--window-body-text-color-dark').trim();
        document.documentElement.style.setProperty('--window-body-text-color-dark', windowBodyTextColor);
        document.documentElement.style.setProperty('--window-body-text-color', windowBodyTextColorDark);

        let border = getComputedStyle(document.documentElement).getPropertyValue('--border').trim();
        let borderDark = getComputedStyle(document.documentElement).getPropertyValue('--border-dark').trim();
        document.documentElement.style.setProperty('--border-dark', border);
        document.documentElement.style.setProperty('--border', borderDark);

        let scrollbarTrackColor = getComputedStyle(document.documentElement).getPropertyValue('--scrollbar-track-color').trim();
        let scrollbarTrackColorDark = getComputedStyle(document.documentElement).getPropertyValue('--scrollbar-track-color-dark').trim();
        document.documentElement.style.setProperty('--scrollbar-track-color-dark', scrollbarTrackColor);
        document.documentElement.style.setProperty('--scrollbar-track-color', scrollbarTrackColorDark);

        let scrollbarButtonColor = getComputedStyle(document.documentElement).getPropertyValue('--scrollbar-button-color').trim();
        let scrollbarButtonColorDark = getComputedStyle(document.documentElement).getPropertyValue('--scrollbar-button-color-dark').trim();
        document.documentElement.style.setProperty('--scrollbar-button-color-dark', scrollbarButtonColor);
        document.documentElement.style.setProperty('--scrollbar-button-color', scrollbarButtonColorDark);

        let scrollbarButtonColorHover = getComputedStyle(document.documentElement).getPropertyValue('--scrollbar-button-color-hover').trim();
        let scrollbarButtonColorHoverDark = getComputedStyle(document.documentElement).getPropertyValue('--scrollbar-button-color-hover-dark').trim();
        document.documentElement.style.setProperty('--scrollbar-button-color-hover-dark', scrollbarButtonColorHover);
        document.documentElement.style.setProperty('--scrollbar-button-color-hover', scrollbarButtonColorHoverDark);

        let tabBackgroundColor = getComputedStyle(document.documentElement).getPropertyValue('--tab-background-color').trim();
        let tabBackgroundColorDark = getComputedStyle(document.documentElement).getPropertyValue('--tab-background-color-dark').trim();
        document.documentElement.style.setProperty('--tab-background-color-dark', tabBackgroundColor);
        document.documentElement.style.setProperty('--tab-background-color', tabBackgroundColorDark);

        let tabTextColor = getComputedStyle(document.documentElement).getPropertyValue('--tab-text-color').trim();
        let tabTextColorDark = getComputedStyle(document.documentElement).getPropertyValue('--tab-text-color-dark').trim();
        document.documentElement.style.setProperty('--tab-text-color-dark', tabTextColor);
        document.documentElement.style.setProperty('--tab-text-color', tabTextColorDark);

        let tabBackgroundColorHover = getComputedStyle(document.documentElement).getPropertyValue('--tab-background-color-hover').trim();
        let tabBackgroundColorHoverDark = getComputedStyle(document.documentElement).getPropertyValue('--tab-background-color-hover-dark').trim();
        document.documentElement.style.setProperty('--tab-background-color-hover-dark', tabBackgroundColorHover);
        document.documentElement.style.setProperty('--tab-background-color-hover', tabBackgroundColorHoverDark);

        let tabTextColorHover = getComputedStyle(document.documentElement).getPropertyValue('--tab-text-color-hover').trim();
        let tabTextColorHoverDark = getComputedStyle(document.documentElement).getPropertyValue('--tab-text-color-hover-dark').trim();
        document.documentElement.style.setProperty('--tab-text-color-hover-dark', tabTextColorHover);
        document.documentElement.style.setProperty('--tab-text-color-hover', tabTextColorHoverDark);

        let tabBackgroundColorActive = getComputedStyle(document.documentElement).getPropertyValue('--tab-background-color-active').trim();
        let tabBackgroundColorActiveDark = getComputedStyle(document.documentElement).getPropertyValue('--tab-background-color-active-dark').trim();
        document.documentElement.style.setProperty('--tab-background-color-active-dark', tabBackgroundColorActive);
        document.documentElement.style.setProperty('--tab-background-color-active', tabBackgroundColorActiveDark);

        let tabTextColorActive = getComputedStyle(document.documentElement).getPropertyValue('--tab-text-color-active').trim();
        let tabTextColorActiveDark = getComputedStyle(document.documentElement).getPropertyValue('--tab-text-color-active-dark').trim();
        document.documentElement.style.setProperty('--tab-text-color-active-dark', tabTextColorActive);
        document.documentElement.style.setProperty('--tab-text-color-active', tabTextColorActiveDark);

        this.setState({darkMode: !this.state.darkMode});
    }

    render() {
        return (
            <div className="flex-row space-between align-items-center pl-10 border-bottom window-header-background-color">
                <div className="flex-row align-items-center">
                    <img className="mr-10" src={logo} alt="" height="30px" width="30px"/>
                    <ul className="nav navbar-nav">
                        <li><a className="nav-link" href="/operator"><i className="fa-solid fa-house"></i> Главная</a></li>
                        <li><a className="nav-link" href="/reports"><i className="fa-solid fa-file-lines"></i> Отчеты</a></li>
                        <li><a className="nav-link" href="/logout"><i className="fa-solid fa-right-from-bracket"></i> Выход</a></li>
                    </ul>
                </div>
                <div className="flex-row align-items-center g-10">
                    <DarkModeButton/>
                    <ConnectionState/>
                    {this.state.userName &&
                        <div className="flex-row border-right pr-10 pl-10" style={{whiteSpace: "nowrap"}}>
                            {this.state.userName}
                        </div>}
                    <div className="flex-column pl-10 pr-10">
                        <div className="text-align-center">
                            {dateFormatterWithDotDelimeter(this.state.currentDate)}</div>
                        <div className="text-align-center">{timeFormatter(this.state.currentDate)}</div>
                    </div>
                </div>
            </div>
        )
    }
}

export default OperatorNav;

